package sem4.edustreambe.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3Config {

    @Value("${aws.s3.access-key}")
    String accessKey;

    @Value("${aws.s3.secret-key}")
    String secretKey;

    @Value("${aws.s3.region}")
    String region;

    @Value("${aws.s3.endpoint:}")
    String s3Endpoint;

    @Bean
    public S3Client s3Client() {
        if ("dummy-access".equals(accessKey) || accessKey == null || accessKey.trim().isEmpty()) {
            return null;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (s3Endpoint != null && !s3Endpoint.trim().isEmpty()) {
            builder.endpointOverride(java.net.URI.create(s3Endpoint));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        if ("dummy-access".equals(accessKey) || accessKey == null || accessKey.trim().isEmpty()) {
            return null; // Return null if not configured to prevent startup crash
        }
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (s3Endpoint != null && !s3Endpoint.trim().isEmpty()) {
            builder.endpointOverride(java.net.URI.create(s3Endpoint));
        }

        return builder.build();
    }
}
