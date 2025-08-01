package S13P11A708.backend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Service
@RequiredArgsConstructor
public class S3Service {

    // Spring Cloud AWS가 자동 주입
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        String fileName = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(fileName)
//                .contentType(file.getContentType())
//                .build();
//
//        s3Client.putObject(putObjectRequest,
//                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//
//        return getFileUrl(fileName);

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
     * 파일 삭제
     */
    public void deleteFile(String fileName) {

        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
        amazonS3Client.deleteObject(deleteObjectRequest);
        log.info(fileName);
    }

    /**
     * 파일 URL 생성
     */
    private String getFileUrl(String fileName) {
        return String.format("https://%s.s3.us-east-2.amazonaws.com/%s", bucketName, fileName);
    }
}
