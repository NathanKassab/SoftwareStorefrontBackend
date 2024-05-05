package me.bannock.capstone.backend.app.cp;

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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final String PAGE_PATH = "/app/main/myAccount";
    private final UserService userService;

    @PostMapping("genApiKey")
    public ResponseEntity<?> genApiKey(HttpServletResponse response) throws IOException {
        AccountDTO user;
        try{
            user = getCurrentUser();
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

        try {
            userService.genApiKey(user.getUid());
        } catch (UserServiceException e) {
            logger.error("Something went wrong while generating an api key for a user", e);
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }
        response.sendRedirect(PAGE_PATH);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("resetHwid")
    @PreAuthorize("hasAnyAuthority('PRIV_UPDATE_OWN_HWID', 'PRIV_UPDATE_USER_HWIDS', 'PRIV_RESET_HWID')")
    public ResponseEntity<?> resetHwid(HttpServletResponse response) throws IOException {
        AccountDTO user;
        try{
            user = getCurrentUser();
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

        try {
            userService.setHwid(user.getUid(), null);
        } catch (UserServiceException e) {
            logger.error("Something went wrong while resetting a user's hwid", e);
            return ResponseEntity.internalServerError().body(e.getErrorMessage());
        }

        response.sendRedirect(PAGE_PATH);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Gets the currently logged-in user
     * @return The user
     * @throws Exception If something goes wrong while finding the user; also logs the attempt as an error
     */
    private AccountDTO getCurrentUser() throws Exception{
        Optional<AccountDTO> user = userService.getAccountWithUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isEmpty()){
            logger.error("User does not exist, user={}", SecurityContextHolder.getContext().getAuthentication().getName());
            throw new Exception("User does not exist");
        }
        return user.get();
    }

}
