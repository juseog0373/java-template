package com.nexacode.template.file.application;

import com.nexacode.template.file.presentation.FileRequest;
import com.nexacode.template.file.presentation.FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;


    public FileResponse.GetFile uploadFile(MultipartFile file, String originalName, String keyName) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        String filePath = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, keyName);

        return FileResponse.GetFile.builder()
                .originalName(originalName)
                .filePath(filePath)
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFile(FileRequest.DownloadFile request) {
        if (request.getFilePath() == null || request.getFilePath().isBlank()) {
            throw new IllegalArgumentException("filePath는 필수입니다.");
        }

        String key = extractKeyFromUrl(request.getFilePath());
        log.info("Downloading file from {}", key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getObjectRequest);

            String fileName = key.substring(key.lastIndexOf("/") + 1);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(s3Stream));

        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            log.warn("S3에서 해당 키를 찾을 수 없습니다: {}", key);
            throw new IllegalArgumentException("해당 파일을 찾을 수 없습니다. (key: " + key + ")");
        }
    }

    private String extractKeyFromUrl(String url) {

        int lastSlash = url.lastIndexOf("/");
        if (lastSlash == -1 || lastSlash == url.length() - 1) {
            throw new IllegalArgumentException("S3 URL에서 파일 키를 추출할 수 없습니다.");
        }
        return url.substring(lastSlash + 1);
    }
}
