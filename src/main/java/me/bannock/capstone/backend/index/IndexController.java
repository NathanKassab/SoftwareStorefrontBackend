package me.bannock.capstone.backend.index;

import me.bannock.capstone.backend.accounts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    public IndexController(UserService userService){
        this.userService = userService;
    }

    private final UserService userService;

    @GetMapping("helloWorld")
    public String helloWorld(){
        return "helloWorld";
    }

    @GetMapping("login")
    public String login(){
        return "login";
    }

    @GetMapping("register")
    public String register(){
        return "register";
    }

    @PostMapping("register")
    @ResponseBody
    public ResponseEntity<String> processRegister(@RequestParam(name = "username") String username,
                                                  @RequestParam(name = "email") String email,
                                                  @RequestParam(name = "password") String password,
                                                  @RequestParam(name = "confirmPassword") String confirmPassword) {
        if (!password.equals(confirmPassword)){
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        try{
            userService.register(email, username, password);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Account created");
    }

}
