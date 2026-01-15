package com.wrightlabs.marketplace.storage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final StorageProperties storageProperties;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                storageProperties.getAccessKey(),
                storageProperties.getSecretKey());

        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(storageProperties.getRegion()));

        // For Minio or custom S3-compatible endpoint
        if (storageProperties.getEndpoint() != null && !storageProperties.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(storageProperties.getEndpoint()));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                storageProperties.getAccessKey(),
                storageProperties.getSecretKey());

        var builder = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(storageProperties.getRegion()));

        if (storageProperties.getEndpoint() != null && !storageProperties.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(storageProperties.getEndpoint()));
        }

        return builder.build();
    }
}
