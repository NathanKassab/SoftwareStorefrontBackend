package me.bannock.capstone.backend.app;

import jakarta.servlet.http.HttpServletResponse;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/app/myAccount/")
@Secured("PRIV_VIEW_OWN_ACCOUNT_INFORMATION")
public class CtrlPanelMyAccountController {

    @Autowired
    public CtrlPanelMyAccountController(UserService userService){
        this.userService = userService;
    }

    private final Logger logger = LogManager.getLogger();
    private final UserService userService;

    @PostMapping("genApiKey")
    public ResponseEntity<?> genApiKey(HttpServletResponse response) throws IOException {
        Optional<AccountDTO> user = userService.getAccountWithUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isEmpty()){
            logger.error("User does not exist, user={}", SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.internalServerError().body("User does not exist");
        }

        try {
            userService.genApiKey(user.get().getUid());
        } catch (UserServiceException e) {
            logger.error("Something went wrong while generating an api key for a user", e);
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }
        response.sendRedirect("/app/main/myAccount");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
