package code.withHarry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; 
    }
    
    
    @GetMapping("/index.html")
    public String index() {
        return "index"; 
    }
}