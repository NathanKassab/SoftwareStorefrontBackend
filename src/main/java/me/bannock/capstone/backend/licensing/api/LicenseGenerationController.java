package me.bannock.capstone.backend.licensing.api;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import me.bannock.capstone.backend.products.service.ProductDTO;
import me.bannock.capstone.backend.products.service.ProductService;
import me.bannock.capstone.backend.products.service.ProductServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/licensing/1/")
public class LicenseGenerationController {

    @Autowired
    public LicenseGenerationController(LicenseService licenseService, ProductService productService,
                                       UserService userService, UserDetailsService userDetailsService){
        this.licenseService = licenseService;
        this.productService = productService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    private final Logger logger = LogManager.getLogger();
    private final LicenseService licenseService;
    private final ProductService productService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @GetMapping("generate/{userId}/{productId}/{keygenId}")
    public ResponseEntity<?> createLicense(HttpServletRequest request,
                                           @PathVariable(name = "userId") long uid,
                                           @PathVariable(name = "productId") long productId,
                                           @PathVariable(name = "keygenId") String keygenId){

        // We need to verify that the requester is authorized without using the authorization header
        // because external tools may need to generate license keys; they don't always support
        // customizing headers
        try {
            Optional<ProductDTO> product = productService.getProductDetails(productId);
            if (product.isEmpty())
                return ResponseEntity.badRequest().body("Product id %s does not exist".formatted(productId));

            long ownerId = product.get().getOwnerUid();
            if (ownerId != uid) {
                logger.warn("User {} attempted to create license with bad user id, " +
                                "providedUid={}, actualUid={}, productId={}, sessionId={}",
                        request.getRemoteAddr(), uid, ownerId, productId, request.getSession().getId());
                return ResponseEntity.badRequest().body("Bad user ID");
            }

            String productKeygenId = product.get().getKeygenId();
            if (!keygenId.equals(productKeygenId)) {
                logger.warn("User {} attempted to create license with bad keygen id, " +
                                "providedId={}, actualId={}, productId={}, sessionId={}",
                        request.getRemoteAddr(), keygenId, productKeygenId, productId, request.getSession().getId());
                return ResponseEntity.badRequest().body("Bad keygen id");
            }

            // Last check to ensure product should actually be generating licenses
            if (product.get().isHidden() || product.get().isDisabled())
                return ResponseEntity.badRequest().body("Product is %s".formatted(product.get().isHidden() ? "hidden" : "disabled"));

        } catch (ProductServiceException e) {
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }

        // The service layer methods are protected by spring security, so we need to authorize this request
        // with the user's account
        Optional<AccountDTO> user = userService.getAccountWithUid(uid);
        if (user.isEmpty())
            return ResponseEntity.badRequest().body("Owner uid %s does not resolve to a registered account".formatted(uid));

        // Now we grab the api key from the user and authenticate using that
        String apiKey = user.get().getApiKey();
        try {
            userService.loginWithApiKey(apiKey);
        } catch (UserServiceException e) {
            return ResponseEntity.badRequest().body(e.getErrorMessage());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.get().getEmail());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities())
        );

        // We can now generate the license now that our request is authenticated
        try {
           String license = licenseService.createLicense(productId);
           logger.info("User has created a new license key, uid={}, productId={}, sessionId={}",
                    uid, productId, request.getSession().getId());
            return ResponseEntity.ok(license);
        } catch (LicenseServiceException e) {
            logger.warn("Something went wrong, sessionId=%s".formatted(request.getSession().getId()), e);
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }

    }

}
