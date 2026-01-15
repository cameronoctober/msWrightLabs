package com.wrightlabs.marketplace.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {
    private String type;
    private String bucket;
    private String region;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private int presignedUrlExpiryMinutes = 60;
}
