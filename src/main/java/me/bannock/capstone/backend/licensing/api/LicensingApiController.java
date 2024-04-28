package me.bannock.capstone.backend.licensing.api;

import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.licensing.service.LicenseDTO;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import me.bannock.capstone.backend.products.service.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/licensing/1/")
@Secured("PRIV_USE_API")
public class LicensingApiController {

    @Autowired
    public LicensingApiController(LicenseService licenseService, ProductService productService, UserService userService){
        this.licenseService = licenseService;
        this.userService = userService;
        this.productService = productService;
    }

    private final Logger logger = LogManager.getLogger();
    private final LicenseService licenseService;
    private final ProductService productService;
    private final UserService userService;

    @PostMapping("activate")
    @Secured("PRIV_ACTIVATE_LICENSE")
    public ResponseEntity<?> activateLicense(@RequestParam(name = "uid") long userId,
                                             @RequestParam(name = "license") String license){
        // We check that the user exists in our system as our
        // licensing service does not handle for that
        Optional<AccountDTO> account = userService.getAccountWithUid(userId);
        if (account.isEmpty()) {
            logger.warn("Could not activate license because uid does not exist in our systems");
            return ResponseEntity.badRequest().body("uid does not exist");
        }

        try {
            licenseService.activateLicense(userId, license);
            Optional<LicenseDTO> licenseDTO = licenseService.getLicense(license);
            if (licenseDTO.isEmpty()){
                logger.error("The license that we just activated no longer exists?");
                return ResponseEntity.internalServerError().build();
            }

            logger.info("User activated license");
            return ResponseEntity.ok(licenseDTO.get());
        } catch (LicenseServiceException e) {
            logger.warn("Could not activate license due to internal service error", e);
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }
    }

}
