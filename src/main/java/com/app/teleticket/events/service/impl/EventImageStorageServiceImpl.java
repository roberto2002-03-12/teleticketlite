package com.app.teleticket.events.service.impl;

import com.app.teleticket.events.exception.EventException;
import com.app.teleticket.events.service.EventImageStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EventImageStorageServiceImpl implements EventImageStorageService {

    private static final String IMAGE_PREFIX = "event-images/";

    @Inject
    S3Client s3;

    @Inject
    @ConfigProperty(name = "s3.bucket-name")
    String bucketName;

    @Inject
    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;

    @Override
    public String upload(Integer eventId, Integer index, String contentType, byte[] bytes) {
        String extension = extensionFromContentType(contentType);
        String objectKey = IMAGE_PREFIX + eventId + "/" + index + "-" + UUID.randomUUID() + "." + extension;
        try {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(bytes));
        } catch (S3Exception e) {
            throw new EventException(502, "S3 upload failed: " + e.getMessage());
        }
        return publicUrl(objectKey);
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new EventException(502, "S3 delete failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            try {
                s3.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());
            } catch (S3Exception e) {
                throw new EventException(502, "S3 batch delete failed: " + e.getMessage());
            }
        }
    }

    private String publicUrl(String objectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new EventException(415, "Only jpg, jpeg and png images are allowed");
        };
    }
}