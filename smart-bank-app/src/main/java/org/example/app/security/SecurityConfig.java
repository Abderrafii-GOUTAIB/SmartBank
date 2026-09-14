package org.example.app.security;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.example.service.persistence.user.UserEntity;
import org.example.service.persistence.user.UserJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserJpaRepository repository) {
        return username -> {
            var user = repository.findByUsername(username).orElseThrow(
                    () -> new UsernameNotFoundException("User not found: " + username)
            );
            return User.withUsername(user.getUsername())
                    .password(user.getPasswordHash())
                    .roles(user.getRole())
                    .build();
        };
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .defaultSuccessUrl("/h2-console/", true)
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                PathRequest.toH2Console(),
                                new AntPathRequestMatcher("/api/v1/auth/login", "POST")
                        )
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }


    @Bean
    @Profile("dev")
    public CommandLineRunner createDevUser(
            UserJpaRepository repository,
            PasswordEncoder encoder,
            @Value("${SMARTBANK_LOGIN_PASSWORD}") String password) {

        return args -> {
            if (repository.findByUsername("abderrafii").isEmpty()) {
                repository.save(new UserEntity(
                        "abderrafii",
                        encoder.encode(password),
                        "CUSTOMER"
                ));
            }
        };
    }


    @Bean
    public SecretKey jwtSecretKey(@Value("${SMARTBANK_JWT_SECRET}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT signing key must contain at least 32 bytes");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtSigningKey));
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

}
