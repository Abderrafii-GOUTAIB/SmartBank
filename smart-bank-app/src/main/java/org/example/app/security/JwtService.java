package org.example.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder encoder;

    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String createToken(Authentication authentication) {
        Instant now = Instant.now();

        // 1. Header: the signing method
        var header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        // 2. Payload: information carried by the token
        var claims = JwtClaimsSet.builder()
                .issuer("smartbank")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .build();

        // 3. Sign and return the complete token
        return encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}