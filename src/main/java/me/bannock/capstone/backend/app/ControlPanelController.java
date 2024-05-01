package me.bannock.capstone.backend.app;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.security.Privilege;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/app/")
@Secured("PRIV_VIEW_MAIN_APP_PANEL")
public class ControlPanelController {

    @Autowired
    public ControlPanelController(UserService userService){
        this.userService = userService;

        // This is a simple way of limiting some user to specific pages
        Map<String, Privilege[]> pages = new LinkedHashMap<>();
        pages.put("myAccount", new Privilege[]{Privilege.PRIV_VIEW_OWN_ACCOUNT_INFORMATION});
        pages.put("modifyUserPrivileges", new Privilege[]{Privilege.PRIV_VIEW_USER_PRIVS});
        this.pages = pages;
    }

    private final Logger logger = LogManager.getLogger();
    private final UserService userService;
    private final Map<String, Privilege[]> pages;

    @GetMapping({"", "main", "main/", "main/{page}"})
    public String main(
            HttpServletRequest request,
            @PathVariable(name = "page", required = false) String page,
            @RequestParam(name = "loggedIn", required = false, defaultValue = "false") boolean justLoggedIn,
            Model model
    ){

        // If the user requests a nonexistent page, or if they request a page where they lack
        // needed privileges, we want to default to whatever placeholder page the template uses by
        // setting the page to null. However, if it is already null, we want to ignore that logic
        if (page == null);
        else if (!this.pages.containsKey(page)){
            logger.info("User attempted to open app " +
                            "page that does not exist, user={}, sessionId={}, page={}",
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    request.getSession().getId(), page);
            page = null;
        }else{
            Privilege[] neededPrivs = this.pages.get(page);
            if (!hasPrivs(neededPrivs)){
                logger.info("User attempted to open app " +
                        "page but lacked the needed privileges, user={}, sessionId={}, neededPrivs={}, page={}",
                        SecurityContextHolder.getContext().getAuthentication().getName(),
                        request.getSession().getId(), neededPrivs, page);
                page = null;
            }
        }

        // The sidebar is built using the mapping of pages we have in this class, so we
        // need to create a list of pages that the user is able to access
        List<String> sideBarPages = getPagesUserCanAccess();

        Optional<AccountDTO> userDto = userService.getAccountWithUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (userDto.isEmpty()){
            logger.error("Could not find user account, sessionId={}, username={}",
                    request.getSession().getId(), SecurityContextHolder.getContext().getAuthentication().getName());
            throw new RuntimeException("Could not find user account");
        }

        model.addAttribute("request", request);
        model.addAttribute("sideBarPages", sideBarPages);
        model.addAttribute("page", page);
        model.addAttribute("justLoggedIn", justLoggedIn);
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication());
        model.addAttribute("userDto", userDto.get());
        return "app/controlPanel";
    }

    /**
     * @return The list of pages that the currently logged-in user can access
     */
    public List<String> getPagesUserCanAccess(){
        List<String> accessiblePages = new ArrayList<>();
        for (String page : this.pages.keySet()){
            if (hasPrivs(this.pages.get(page))){
                accessiblePages.add(page);
            }
        }
        return accessiblePages;
    }

    /**
     * Checks if the currently logged-in user has a all the privileges in the provided array
     * @param neededPrivs The provided array of privileges
     * @return True if the user has all the listed privileges
     */
    public boolean hasPrivs(Privilege[] neededPrivs){
        return Arrays.stream(neededPrivs).allMatch(priv -> {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(userPriv -> {
                return userPriv.getAuthority().equals(priv.getPrivilege());
            });
        });
    }

}
