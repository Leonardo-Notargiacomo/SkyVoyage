package io.github.fontysvenlo.ais.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping
    public String login(@RequestParam String email, @RequestParam String password) {
        if (userService.authenticate(email, password)) {
            return "Login successful";
        } else {
            return "Invalid email or password";
        }
    }
}