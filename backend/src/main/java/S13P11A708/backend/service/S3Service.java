package S13P11A708.backend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    // Spring Cloud AWS가 자동 주입
    private final AmazonS3Client amazonS3Client;
    private final RestTemplate restTemplate;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * MultipartFile 업로드
     */
    public String uploadMultipartFile(MultipartFile file, String directory) throws IOException {
        String fileName = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // ObjectMetadata 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // S3에 파일 업로드
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
        );

        amazonS3Client.putObject(putObjectRequest);

        log.info(fileName);
        return getFileUrl(fileName);
    }

    /**
     * byte[] 데이터 업로드 (PDF, 이미지 등)
     */
    public String uploadByteDataFile(String fileName, byte[] fileData, String contentType) {
        try {
            // ObjectMetadata 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileData.length);

            // ByteArrayInputStream으로 변환
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);

            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream,
                    metadata
            );

            amazonS3Client.putObject(putObjectRequest);

            log.info("파일 업로드 완료: " + fileName);
            return getFileUrl(fileName);

        } catch (Exception e) {
            log.error("파일 업로드 실패: "+ fileName + " error: " + e.getMessage());
            throw new RuntimeException("S3 파일 업로드에 실패했습니다. " + fileName, e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileName) {

        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
        amazonS3Client.deleteObject(deleteObjectRequest);
        log.info(fileName);
    }

    /**
     * URL에서 PDF를 다운로드하고 S3에 업로드
     */
    public String downloadAndUploadPdf(String pdfUrl, String s3FileName) {
        try {
            // PDF URL에서 데이터 다운로드
            ResponseEntity<byte[]> response =  restTemplate.getForEntity(pdfUrl, byte[].class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() ==  null) {
                throw new RuntimeException("PDF 다운로드에 실패했습니다. Status: " + response.getStatusCode());
            }

            byte[] pdfData = response.getBody();

            // S3에 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(pdfData.length);
            metadata.setContentType("application/pdf");
            metadata.setContentDisposition("inline; filename=\"" + s3FileName + "\"");

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfData)) {
                amazonS3Client.putObject(bucketName, s3FileName, inputStream, metadata);
            }

            String s3Url = amazonS3Client.getUrl(bucketName, s3FileName).toString();

            return s3Url;

        } catch (Exception e) {
            throw new RuntimeException("PDF 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 URL 생성
     */
    private String getFileUrl(String fileName) {
        return String.format("https://%s.s3.us-east-2.amazonaws.com/%s", bucketName, fileName);
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 파일 URL입니다.");
        }

        try {
            // S3 URL 패턴에서 파일명 추출
            String baseUrl = String.format("https://%s.s3.us-east-2.amazonaws.com/", bucketName);
            if (fileUrl.startsWith(baseUrl)) {
                return fileUrl.substring(baseUrl.length());
            }

            // 일반적인 URL에서 파일명 추출
            int lastSlashIndex = fileUrl.lastIndexOf("/");
            if (lastSlashIndex != -1 && lastSlashIndex < fileUrl.length() - 1) {
                return fileUrl.substring(lastSlashIndex + 1);
            }

            throw new IllegalArgumentException("파일명을 추출할 수 없는 URL 형식입니다.");

        } catch (Exception e) {
            log.error("URL에서 파일명 추출 실패: " + fileUrl);
            throw new IllegalArgumentException("파일명 추출에 실패했습니다: " + fileUrl, e);
        }
    }
}