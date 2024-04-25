package me.bannock.capstone.backend.app.admin;


import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/admin/")
@Secured("PRIV_VIEW_ADMIN_PANEL")
public class AdminController {

    @GetMapping("main")
    public String main(Model model){
        return "helloWorld";
    }

}
