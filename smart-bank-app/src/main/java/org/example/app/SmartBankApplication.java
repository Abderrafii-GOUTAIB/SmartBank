package org.example.app;


import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = "org.example")
@ConfigurationPropertiesScan(basePackages = "org.example")
public class SmartBankApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartBankApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartBankApplication.class, args);
    }

    @Bean
    CommandLineRunner logRuntimeProperties(Environment env) {
        return args -> {
            log.warn("[PROFILE-CHECK] Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
            log.warn("[PROFILE-CHECK] smartbank.data.path = {}", env.getProperty("smartbank.data.path", "NOT_SET"));
            log.warn("[PROFILE-CHECK] app.profile.marker = {}", env.getProperty("app.profile.marker", "NOT_SET"));
        };
    }
}
