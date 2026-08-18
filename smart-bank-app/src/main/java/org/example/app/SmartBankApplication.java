package org.example.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.example")
public class SmartBankApplication {

    public static void main(String[] args){
        SpringApplication.run(SmartBankApplication.class, args);
    }
}
