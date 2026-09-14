package org.example.app.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request) {

        var credentials =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(),
                        request.password()
                );

        var authentication =
                authenticationManager.authenticate(credentials);

        String token = jwtService.createToken(authentication);

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(new TokenResponse(token, "Bearer"));
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String tokenType) {
    }
}