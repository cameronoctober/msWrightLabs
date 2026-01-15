package com.wrightlabs.marketplace.storage.service;

import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.ProductFile;
import com.wrightlabs.marketplace.domain.repository.ProductFileRepository;
import com.wrightlabs.marketplace.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;
    private final ProductFileRepository productFileRepository;

    /**
     * Upload a file to S3 and create ProductFile record
     */
    @Transactional
    public ProductFile uploadFile(MultipartFile file, Product product, boolean isPreview) throws IOException {
        String fileKey = generateFileKey(product.getId(), file.getOriginalFilename());

        // Upload to S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        log.info("Uploaded file to S3: {}", fileKey);

        // Create database record
        ProductFile productFile = ProductFile.builder()
                .product(product)
                .filename(file.getOriginalFilename())
                .fileKey(fileKey)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .isPreview(isPreview)
                .build();

        return productFileRepository.save(productFile);
    }

    /**
     * Generate a presigned URL for downloading a file
     */
    public String generateDownloadUrl(String fileKey) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(storageProperties.getPresignedUrlExpiryMinutes()))
                .getObjectRequest(req -> req
                        .bucket(storageProperties.getBucket())
                        .key(fileKey))
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        String url = presignedRequest.url().toString();

        log.debug("Generated presigned URL for file: {}", fileKey);
        return url;
    }

    /**
     * Delete a file from S3 and database
     */
    @Transactional
    public void deleteFile(Long fileId) {
        ProductFile productFile = productFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        // Delete from S3
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(productFile.getFileKey())
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("Deleted file from S3: {}", productFile.getFileKey());

        // Delete database record
        productFileRepository.delete(productFile);
    }

    /**
     * Generate a unique file key for S3
     */
    private String generateFileKey(Long productId, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("products/%d/%s-%s", productId, uuid, sanitizedFilename);
    }
}
