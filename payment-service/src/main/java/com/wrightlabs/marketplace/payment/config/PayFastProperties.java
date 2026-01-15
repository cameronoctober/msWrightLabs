package com.wrightlabs.marketplace.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payfast")
@Data
public class PayFastProperties {
    private String merchantId;
    private String merchantKey;
    private String passphrase;
    private boolean sandboxMode;
    private String baseUrl;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;
}
