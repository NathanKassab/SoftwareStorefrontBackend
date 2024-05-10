package me.bannock.capstone.backend.products.download.api;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.products.download.service.ProductDownloadDTO;
import me.bannock.capstone.backend.products.download.service.ProductDownloadService;
import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import me.bannock.capstone.backend.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/products/download/1/")
public class ProductDownloadController {

    @Autowired
    public ProductDownloadController(ProductService productService, UserService userService,
                                     LicenseService licenseService, ProductDownloadService productDownloadService){
        this.productService = productService;
        this.userService = userService;
        this.licenseService = licenseService;
        this.productDownloadService = productDownloadService;
    }

    private final Logger logger = LogManager.getLogger();
    private final ProductService productService;
    private final UserService userService;
    private final LicenseService licenseService;
    private final ProductDownloadService productDownloadService;

    @GetMapping("")
    public ResponseEntity<?> download(HttpServletRequest request,
                                      @RequestParam(name = "productId") long productId){
        Optional<ProductDTO> product;
        try {
            product = productService.getProductDetails(productId);
        } catch (ProductServiceException e) {
            logger.error("Something went wrong while grabbing product details, productId={}, sessionId={}",
                    productId, request.getSession().getId());
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }
        if (product.isEmpty())
            return ResponseEntity.badRequest().body("Product does not exist");
        if (product.get().isDisabled() || !product.get().isApproved())
            return ResponseEntity.badRequest().body("Product is unavailable at this time");

        // We need the user to check if they own a license for the product they are downloading
        Optional<AccountDTO> user = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (user.isEmpty())
            return ResponseEntity.badRequest().body("Could not find your user account");

        if (!licenseService.ownsProduct(user.get().getUid(), productId))
            return ResponseEntity.status(401).body("Your user is not authorized to download this product");

        Optional<ProductDownloadDTO> downloadDto = productDownloadService.getProductDownload(productId);
        if (downloadDto.isEmpty())
            return ResponseEntity.internalServerError().body("Download for this product is unavailable at this time. " +
                    "Please try again later");

        ByteArrayResource resource = new ByteArrayResource(downloadDto.get().getBytes());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(downloadDto.get().getFileName()));
        return ResponseEntity.ok()
                .contentLength(downloadDto.get().getBytes().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(responseHeaders)
                .body(resource);
    }

}
