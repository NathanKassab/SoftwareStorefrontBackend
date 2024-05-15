package me.bannock.capstone.backend.accounts.api;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import me.bannock.capstone.backend.utils.ControllerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts/1/")
@Secured("PRIV_USE_API")
public class AccountApiController {

    @Autowired
    public AccountApiController(UserService userService){
        this.userService = userService;
    }

    private final Logger logger = LogManager.getLogger();
    private final UserService userService;

    @GetMapping("login")
    @PreAuthorize("hasAuthority('PRIV_LOGIN') AND hasAuthority('PRIV_LAUNCH_LOADER')") // Login is called when the user first launches the loader
    public ResponseEntity<?> login(HttpServletRequest request,
                                   @RequestParam(name = "hwid") String hwid){
        Optional<AccountDTO> user = ControllerUtils.getUserDtoFromAuthenticatedRequest(userService);
        if (user.isEmpty()){
            logger.warn("Unable to find user account, sessionId={}", request.getSession().getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not find your user account");
        }

        try {
            if (userService.doesHwidMatch(hwid, user.get().getUid())){
                logger.info("User logged in, sessionId={}, user={}", request.getSession().getId(), user.get());
                return ResponseEntity.ok(user.get());
            }else{
                logger.warn("User attempted to login, but their hwid did not match, sessionId={}, hwid={}, user={}",
                        request.getSession().getId(), hwid, user.get());
                return ResponseEntity.badRequest().body("HWID does not match");
            }
        } catch (UserServiceException e) {
            logger.error("Could not check if hwid matches, sessionId={}, hwid={}, uid={}",
                    request.getSession().getId(), hwid, user.get().getUid(), e);
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }
    }

}
