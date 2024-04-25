package me.bannock.capstone.backend.app;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/")
@Secured("PRIV_VIEW_MAIN_APP_PANEL")
public class AppController {

    @GetMapping("main")
    public String main(Model model){
        return "helloWorld";
    }

}
