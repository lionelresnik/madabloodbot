package com.chatbot.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;


/**
 * @author Lionel Resnik
 */
@Service
public class AmazonS3Service {
    private static final Logger   log = LoggerFactory.getLogger(AmazonS3Service.class);
    private              S3Client s3;


    public AmazonS3Service(@Value("${aws.region}") String region) {

        try {
            s3 = S3Client.builder().region(Region.of(region)).build();
        }
        catch (SdkClientException e) {
            log.error("Failed to connect to Amazon S3: {}", e.getMessage());
        }
    }


    public void uploadFile(String bucketName, String key, Path file) {
        if (s3 == null) {
            log.error("Cannot upload file to Amazon S3 because the connection was not established");
            return;
        }
        try {
            RetryConfig config = RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofSeconds(1)).build();

            Retry retry = Retry.of("id", config);

            CheckedRunnable putObjectRunnable = Retry.decorateCheckedRunnable(retry, () -> {
                log.info("Attempting to upload file to Amazon S3: {}", file);
                s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                             RequestBody.fromFile(file));
            });

            Try.run(putObjectRunnable).onFailure(e -> log.error("Failed to upload file to Amazon S3: {}", file, e));
        }
        catch (SdkClientException e) {
            // Log the error and continue with the application
            log.error("Failed to download file from Amazon S3", e);
        }
    }

    public Path downloadFile(String bucketName, String key) throws IOException {
        if (s3 == null) {
            log.error("Cannot download file from Amazon S3 because the connection was not established");
            return null;
        }

        try {
            RetryConfig config = RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofSeconds(1)).build();

            Retry retry = Retry.of("id", config);

            Path file = Files.createTempFile("medications", ".json");

            CheckedFunction1<Path, Path> downloadFile = Retry.decorateCheckedFunction(retry, (Path path) -> {
                log.info("Attempting to download file from Amazon S3: {}", key);
                s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build(),
                             ResponseTransformer.toFile(path));
                return path;
            });

            return Try.of(() -> downloadFile.apply(file))
                      .onFailure(e -> log.error("Failed to download file from Amazon S3: {}", key, e)).get();
        }
        catch (SdkClientException e) {
            // Log the error and continue with the application
            log.error("Failed to download file from Amazon S3", e);
        }

        return null;
    }
}
