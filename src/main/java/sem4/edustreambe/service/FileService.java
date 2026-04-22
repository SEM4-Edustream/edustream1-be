package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileService {

    final S3Presigner s3Presigner;
    final S3Client s3Client;

    @Value("${aws.s3.region:ap-southeast-2}")
    String region;

    @Value("${aws.s3.bucket}")
    String videoBucket;

    @Value("${aws.s3.document-bucket}")
    String documentBucket;

    public sem4.edustreambe.dto.common.FileResponse generatePresignedUploadUrl(String originalFileName, String contentType, sem4.edustreambe.enums.BucketType type) {
        if (s3Presigner == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // "S3 is not configured"
        }

        // Chọn bucket dựa trên loại
        String targetBucket = (type == sem4.edustreambe.enums.BucketType.DOCUMENT) ? documentBucket : videoBucket;

        // Tạo khóa định danh riêng biệt chống trùng lặp file name
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName.replaceAll("\\s+", "_");

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(targetBucket)
                .key(uniqueFileName)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        // URL để truy cập file sau khi upload
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", targetBucket, region, uniqueFileName);

        return sem4.edustreambe.dto.common.FileResponse.builder()
                .uploadUrl(presignedRequest.url().toString())
                .fileUrl(fileUrl)
                .build();
    }
    public void deleteFile(String fileUrl) {
        if (s3Client == null || fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Parse URL: https://bucket-name.s3.region.amazonaws.com/key
            // Hoặc parse từ format: https://s3.region.amazonaws.com/bucket-name/key (tùy config)
            // Trong FileService line 59: https://%s.s3.%s.amazonaws.com/%s
            
            String key = null;
            String bucket = null;

            if (fileUrl.contains(".s3.")) {
                String temp = fileUrl.replace("https://", "");
                bucket = temp.split("\\.")[0];
                key = temp.substring(temp.indexOf("/") + 1);
            }

            if (bucket != null && key != null) {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
                // log.info("Deleted file from S3: {}", fileUrl);
            }
        } catch (Exception e) {
            // log.error("Failed to delete file from S3: {}", fileUrl, e);
            // Không throw exception để không làm gián đoạn luồng chính (chỉ là dọn dẹp)
        }
    }
}
