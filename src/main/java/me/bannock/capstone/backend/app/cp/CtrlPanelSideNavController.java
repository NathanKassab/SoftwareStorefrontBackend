package me.bannock.capstone.backend.app.cp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import me.bannock.capstone.backend.app.ControlPanelController;
import me.bannock.capstone.backend.licensing.service.LicenseDTO;
import me.bannock.capstone.backend.licensing.service.LicenseService;
import me.bannock.capstone.backend.licensing.service.LicenseServiceException;
import me.bannock.capstone.backend.security.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                                      HttpServletResponse response,
                                      @RequestParam(name = "license") String license) throws IOException {
        // We need to get this user's uid to redeem a license, so we grab their account
        Optional<AccountDTO> userDto = userService.getAccountWithUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (userDto.isEmpty()){
            logger.error("Could not find user account, sessionId={}, username={}",
                    request.getSession().getId(), SecurityContextHolder.getContext().getAuthentication().getName());
            response.sendRedirect("/app/" + ControlPanelController.ERROR_PAGE + "?errorMessage=%s".formatted(URLEncoder.encode("Could not find user account", StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().build();
        }
        AccountDTO user = userDto.get();

        if (user.isDisabled() || user.isLocked() || user.isPasswordExpired() || user.isExpired()){
            logger.info(
                    "Disabled user attempted to activate a license, userDisabled={}, userLocked={}, userExpiredPass={}, userExpired={}, user={}, sessionId={}",
                    user.isDisabled(), user.isLocked(), user.isPasswordExpired(), user.isExpired(), user, request.getSession().getId()
            );
            response.sendRedirect("/app/" + ControlPanelController.ERROR_PAGE + "?errorMessage=%s".formatted(URLEncoder.encode("Your account is not active", StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().build();
        }

        try {
            licenseService.activateLicense(user.getUid(), license);
            Optional<LicenseDTO> licenseDto = licenseService.getLicense(license);
            if (licenseDto.isEmpty())
                throw new RuntimeException("Could not find license key after activating");
            response.sendRedirect("/app/product?productId=%s".formatted(licenseDto.get().getProductId()));
        } catch (LicenseServiceException e) {
            logger.warn("User was unable to activate license key, uid=%s, sessionId=%s".formatted(user.getUid(), request.getSession().getId()), e);
            response.sendRedirect("/app/" + ControlPanelController.ERROR_PAGE + "?errorMessage=%s".formatted(URLEncoder.encode("Failed to activate license: %s".formatted(e.getErrorMessage()), StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().build();
        }

        // Now that the license has been redeemed, we're also going to
        // add shopper privileges to the user
        Role.ROLE_SHOPPER.getPrivileges().forEach(priv -> {
            if (user.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals(priv.getPrivilege()))) {
                try {
                    userService.grantPrivilege(user.getUid(), priv.getPrivilege());
                } catch (UserServiceException e) {
                    logger.error("Failed to grant privilege to user, error={}, uid={}", e, user.getUid());
                }
            }
        });

        // Refreshes privs on app without the need to relog
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        if (user.getApiKey() == null) {
            try {
                userService.genApiKey(user.getUid());
            } catch (UserServiceException e) {
                logger.error("Failed to generate new api key for user. They may not be able to use the loader at this time. error={}, uid={}",
                        e.getErrorMessage(), user.getUid());
            }
        }

        logger.info("User has activated license key, uid={}, license={}", user.getUid(), license);
        return ResponseEntity.status(205).build();
    }

}
