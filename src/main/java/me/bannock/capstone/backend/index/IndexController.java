package me.bannock.capstone.backend.index;

import jakarta.servlet.http.HttpServletResponse;
import me.bannock.capstone.backend.accounts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    public IndexController(UserService userService){
        this.userService = userService;
    }

    private final UserService userService;

    @GetMapping("")
    public void helloWorld(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login");
    }

    @GetMapping("login")
    public String login(@RequestParam(name = "error", required = false) String error,
                        @RequestParam(name = "notice", required = false) String notice,
                        Model model){
        model.addAttribute("error", error);
        model.addAttribute("notice", notice);
        return "login";
    }

    @GetMapping("register")
    public String register(@RequestParam(name = "error", required = false) String error,
                           Model model){
        model.addAttribute("error", error);
        return "register";
    }

    @PostMapping("register")
    @ResponseBody
    public ResponseEntity<String> processRegister(HttpServletResponse response,
                                                  @RequestParam(name = "username") String username,
                                                  @RequestParam(name = "email") String email,
                                                  @RequestParam(name = "password") String password,
                                                  @RequestParam(name = "confirmPassword") String confirmPassword) throws IOException {
        if (!password.equals(confirmPassword)){
            response.sendRedirect("/register?error=%s".formatted(URLEncoder.encode("Passwords do not match", StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        try{
            userService.register(email, username, password);
        }catch (Exception e){
            response.sendRedirect("/register?error=%s".formatted(URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8)));
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        response.sendRedirect("/login?notice=%s".formatted(URLEncoder.encode("Successfully created user account", StandardCharsets.UTF_8)));
        return ResponseEntity.ok("Account created");
    }

}
