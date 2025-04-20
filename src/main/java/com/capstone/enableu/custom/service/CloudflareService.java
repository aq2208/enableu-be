package com.capstone.enableu.custom.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PresignedUrlUploadRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CloudflareService {
    private final AmazonS3 s3Client;
    @Value("${application.bucket.name}")
    private String bucketName;

    public CloudflareService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String generatePresignedUrl(String objectKey) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(com.amazonaws.HttpMethod.PUT)
                        .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

}
