package net.binder.api.image.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3Manager {
    private final AmazonS3 amazonS3;

    private final FileManager fileManager;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.dir}")
    private String dir;

    public String upload(MultipartFile multipartFile, String storeName) {

        try (InputStream inputStream = multipartFile.getInputStream()) {
            String bucketKey = getBucketKey(storeName);
            ObjectMetadata objectMetadata = getObjectMetadata(multipartFile);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, bucketKey, inputStream
                    , objectMetadata);

            amazonS3.putObject(putObjectRequest);

            return amazonS3.getUrl(bucketName, bucketKey).toString();

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생하였습니다.", e);
        }
    }

    public void delete(String storeName) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, getBucketKey(storeName));

        amazonS3.deleteObject(deleteObjectRequest);
    }

    private String getBucketKey(String storeName) {
        return dir + storeName;
    }

    private ObjectMetadata getObjectMetadata(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());
        return objectMetadata;
    }

}
