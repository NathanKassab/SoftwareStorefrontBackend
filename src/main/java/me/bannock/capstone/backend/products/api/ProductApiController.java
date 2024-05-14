package me.bannock.capstone.backend.products.api;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.licensing.service.LicenseDTO;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import me.bannock.capstone.backend.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products/1/")
@Secured("PRIV_USE_API")
public class ProductApiController {

    @Autowired
    public ProductApiController(UserService userService, ProductService productService, LicenseService licenseService){
        this.userService = userService;
        this.productService = productService;
        this.licenseService = licenseService;
    }

    private final Logger logger = LogManager.getLogger();
    private final UserService userService;
    private final ProductService productService;
    private final LicenseService licenseService;

    @GetMapping("getOwnedProducts")
    public ResponseEntity<?> getOwnedProducts(HttpServletRequest request){
        Optional<AccountDTO> user = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (user.isEmpty()) {
            logger.warn("Unable to find user account, sessionId={}", request.getSession().getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not find your user account");
        }

        List<String> licenses = licenseService.getUserLicenses(user.get().getUid());
        List<ProductDTO> products = new ArrayList<>();
        for (String license : licenses){
            Optional<LicenseDTO> licenseDto = licenseService.getLicense(license);
            if (licenseDto.isEmpty())
                continue;

            long productId = licenseDto.get().getProductId();
            Optional<ProductDTO> product;
            try {
                product = productService.getProductDetails(productId);
            } catch (ProductServiceException e) {
                logger.warn("Unable to fetch details for one product, sessionId={}, productId={}, license={}",
                        request.getSession().getId(), productId, license, e);
                continue;
            }

            if (product.isEmpty()){
                logger.warn("Product not found, sessionId={}, productId={}, license={}",
                        request.getSession().getId(), productId, license);
                continue;
            }
            if (product.get().isDisabled() || product.get().isHidden())
                continue;
            products.add(product.get());
        }

        return ResponseEntity.ok(products);
    }

}
