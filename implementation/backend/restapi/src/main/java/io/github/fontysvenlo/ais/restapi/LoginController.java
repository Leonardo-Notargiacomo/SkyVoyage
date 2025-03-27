package io.github.fontysvenlo.ais.restapi;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "http://localhost")
@RestController
@RequestMapping("/login")
public class LoginController {

    private final Map<String, UserModel> users = new HashMap<>();

    public LoginController() {
        // Add a sample user
        UserModel user = new UserModel();
        user.setEmail("user@example.com");
        user.setPassword("password");
        users.put(user.getEmail(), user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserModel loginRequest) {
        UserModel user = users.get(loginRequest.getEmail());
        Map<String, String> response = new HashMap<>();

        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            response.put("message", "Login successful");
        } else {
            response.put("error", "Invalid email or password");
        }

        return ResponseEntity.ok().body(response);
    }
}
