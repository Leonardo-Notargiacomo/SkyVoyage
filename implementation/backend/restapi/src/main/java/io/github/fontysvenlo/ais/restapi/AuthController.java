package io.github.fontysvenlo.ais.restapi;


import io.github.fontysvenlo.ais.businesslogic.api.LoginService;
import io.github.fontysvenlo.ais.datarecords.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173") // adjust for your frontend port
public class AuthController {
    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        boolean success = loginService.login(request.email(), request.password());
        return success ? ResponseEntity.ok("Login successful") : ResponseEntity.status(401).body("Invalid credentials");
    }
}
