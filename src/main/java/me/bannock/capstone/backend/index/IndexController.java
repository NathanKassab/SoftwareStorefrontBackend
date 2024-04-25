package me.bannock.capstone.backend.index;

import me.bannock.capstone.backend.accounts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("/")
public class IndexController {

    @Autowired
    public IndexController(UserService userService){
        this.userService = userService;
    }

    private final UserService userService;

    @GetMapping("helloWorld")
    public String helloWorld(Model model){
        return "helloWorld";
    }

    @GetMapping("login")
    public String login(Model model){
        return "login";
    }

    @GetMapping("register")
    public String register(Model model){
        return "register";
    }

    @PostMapping("register")
    @ResponseBody
    public ResponseEntity<String> processRegister(Model model,
                                                  @RequestParam(name = "username") String username,
                                                  @RequestParam(name = "email") String email,
                                                  @RequestParam(name = "password") String password) {
        try{
            userService.register(email, username, password);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Account created");
    }

}
