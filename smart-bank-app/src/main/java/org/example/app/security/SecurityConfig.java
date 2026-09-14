package org.example.app.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;


@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${SMARTBANK_LOGIN_PASSWORD}") String password) {

        var user = User.withUsername("abderrafii")
                .password(encoder.encode(password))
                .roles("CUSTOMER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

}
