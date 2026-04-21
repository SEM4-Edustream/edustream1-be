package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
        String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", targetBucket, uniqueFileName);

        return sem4.edustreambe.dto.common.FileResponse.builder()
                .uploadUrl(presignedRequest.url().toString())
                .fileUrl(fileUrl)
                .build();
    }
}
