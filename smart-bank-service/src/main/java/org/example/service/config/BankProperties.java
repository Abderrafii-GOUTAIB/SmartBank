package org.example.service.config;// THE RECOMMENDED VERSION (when you create it, in Part B):

// FILE: smart-bank-service/.../service/config/BankProperties.java

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartbank")
public record BankProperties(
        Data data,
        Account account,
        Risk risk
) {
    public record Data(String path) {}
    public record Account(int minimumDeposit) {}
    public record Risk(int largeTransactionThreshold) {}
}