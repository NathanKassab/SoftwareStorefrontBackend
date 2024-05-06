package me.bannock.capstone.backend.products.view;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/products/")
public class ProductsController {

    @Autowired
    public ProductsController(ProductService productService, UserService userService, LicenseService licenseService){
        this.productService = productService;
        this.userService = userService;
        this.licenseService = licenseService;
    }

    private final Logger logger = LogManager.getLogger();
    private final ProductService productService;
    private final UserService userService;
    private final LicenseService licenseService;

    @GetMapping("view/{productId}")
    public String productLander(HttpServletRequest request,
                                @PathVariable(name = "productId") long productId,
                                Model model){

        long productOwnerUid;
        try {
            Optional<ProductDTO> product = productService.getProductDetails(productId);

            // Validate the product and ensure it should be publicly shown
            if (product.isEmpty()){
                logger.info("User attempted to access nonexistent product lander, productId={}, sessionId={}",
                        product, request.getSession().getId());
                return "products/notFound";
            }
            if (product.get().isDisabled() || product.get().isHidden()){
                logger.info("User attempted to access product that is disabled or hidden, productId={}, sessionId={}",
                        productId, request.getSession().getId());
                return "products/unavailable";
            }

            productOwnerUid = product.get().getOwnerUid();
            model.addAttribute("product", product.get());
        } catch (ProductServiceException e) {
            logger.error(
                    "Something went wrong while displaying a product lander page, e={}, productId={}, sessionId={}",
                    e, productId, request.getSession().getId());
            throw new RuntimeException(e);
        }

        // Now that we've verified that the product exists, we need to verify the owner account
        // and grab information from that as well
        Optional<AccountDTO> owner = userService.getAccountWithUid(productOwnerUid);
        if (owner.isEmpty()){
            logger.error("User attempted to access product where owner does not exist; " +
                            "this should never happen, productId={}, ownerUid={}, sessionId={}",
                    productId, productOwnerUid, request.getSession().getId());
            return "products/unavailable";
        }
        model.addAttribute("owner", owner.get());

        if (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated()){
            model.addAttribute("ownsProduct", false);
        }else{
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<AccountDTO> currentUser = userService.getAccountWithUsername(username);
            if (currentUser.isEmpty()){
                logger.warn("Current user does not exist, username={}", username);
                throw new RuntimeException("Current user does not exist, name=%s".formatted(username));
            }
            model.addAttribute("ownsProduct", licenseService.ownsProduct(currentUser.get().getUid(), productId));
        }

        return "products/lander";
    }

}
