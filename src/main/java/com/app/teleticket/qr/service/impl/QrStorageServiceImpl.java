package com.app.teleticket.qr.service.impl;

import com.app.teleticket.qr.dto.QrUploadResult;
import com.app.teleticket.qr.exception.QrException;
import com.app.teleticket.qr.service.QrStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

@ApplicationScoped
public class QrStorageServiceImpl implements QrStorageService {

    private static final String QR_PREFIX = "qr-codes/";
    private static final String CONTENT_TYPE = "image/png";

    @Inject
    S3Client s3;

    @Inject
    @ConfigProperty(name = "s3.bucket-name")
    String bucketName;

    @Inject
    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;

    @Override
    public QrUploadResult upload(Integer userId, Integer eventId, byte[] bytes) {
        String objectKey = QR_PREFIX + userId + "/" + eventId + "-" + UUID.randomUUID() + ".png";
        try {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(CONTENT_TYPE)
                            .build(),
                    RequestBody.fromBytes(bytes));
        } catch (S3Exception e) {
            throw new QrException(502, "S3 upload failed: " + e.getMessage());
        }
        return new QrUploadResult(publicUrl(objectKey), objectKey);
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
            throw new QrException(502, "S3 delete failed: " + e.getMessage());
        }
    }

    private String publicUrl(String objectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
    }
}