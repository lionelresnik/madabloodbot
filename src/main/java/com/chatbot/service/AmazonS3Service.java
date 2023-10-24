package com.chatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Lionel Resnik
 */
@Service
public class AmazonS3Service {
    private static final Logger   log    = LoggerFactory.getLogger(AmazonS3Service.class);
    private static final Logger   logger = LoggerFactory.getLogger(AmazonS3Service.class);
    private final        S3Client s3Client;
    @Value("${medications.file.path}")
    private              String   medicationsFilePath;

    public AmazonS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public boolean uploadFile(String bucketName, String key) {
        Path path = Paths.get(medicationsFilePath);
        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                               RequestBody.fromFile(path));
            logger.info("File uploaded successfully to bucket {} with key {}", bucketName, key);
            return true;
        }
        catch (S3Exception e) {
            logger.error("Error occurred while trying to upload file to bucket {} with key {}. Error: {}", bucketName,
                         key, e.getMessage());
            return false;
        }
    }

    public boolean downloadFile(String bucketName, String key) {
        Path path = Paths.get(medicationsFilePath);
        try {
            s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build(),
                               ResponseTransformer.toFile(path));
            logger.info("File downloaded successfully from bucket {} with key {}", bucketName, key);
            return true;
        }
        catch (S3Exception e) {
            logger.error("Error occurred while trying to download file from bucket {} with key {}. Error: {}",
                         bucketName, key, e.getMessage());
            return false;
        }
    }
}


