package me.bannock.capstone.backend.app.cp;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/app/sideNav/")
public class CtrlPanelSideNavController {

    @Autowired
    public CtrlPanelSideNavController(UserService userService, LicenseService licenseService){
        this.userService = userService;
        this.licenseService = licenseService;
    }

    private final Logger logger = LogManager.getLogger();
    private final UserService userService;
    private final LicenseService licenseService;

    @PostMapping("activate")
    @Secured("PRIV_ACTIVATE_LICENSE")
    public ResponseEntity<?> activate(HttpServletRequest request,
                                      @RequestParam(name = "license") String license){

        // We need to get this user's uid to redeem a license, so we grab their account
        Optional<AccountDTO> userDto = userService.getAccountWithUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (userDto.isEmpty()){
            logger.error("Could not find user account, sessionId={}, username={}",
                    request.getSession().getId(), SecurityContextHolder.getContext().getAuthentication().getName());
            throw new RuntimeException("Could not find user account");
        }
        AccountDTO user = userDto.get();

        if (user.isDisabled() || user.isLocked() || user.isPasswordExpired() || user.isExpired()){
            logger.info(
                    "Disabled user attempted to activate a license, userDisabled={}, userLocked={}, userExpiredPass={}, userExpired={}, user={}, sessionId={}",
                    user.isDisabled(), user.isLocked(), user.isPasswordExpired(), user.isExpired(), user, request.getSession().getId()
            );
            return ResponseEntity.badRequest().body("Your account is not active");
        }

        try {
            licenseService.activateLicense(user.getUid(), license);
        } catch (LicenseServiceException e) {
            logger.warn("User was unable to activate license key, uid=%s, sessionId=%s".formatted(user.getUid(), request.getSession().getId()), e);
            return ResponseEntity.internalServerError().body("Failed to activate license: %s".formatted(e.getErrorMessage()));
        }

        return ResponseEntity.status(205).build(); // http 205 is reset content
    }

}
