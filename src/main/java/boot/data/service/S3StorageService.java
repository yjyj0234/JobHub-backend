package boot.data.service; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3;
    private final String bucket;
    private final String region;

    public S3StorageService(S3Client s3,
                            @Value("${cloud.aws.s3.bucket}") String bucket,
                            @Value("${cloud.aws.region}") String region) {
        this.s3 = s3;
        this.bucket = bucket;
        this.region = region;
    }

    public UploadResult upload(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String key = "jobpostings/" + datePath + "/" + UUID.randomUUID() + "_" + original;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        return new UploadResult(key, url, original, file.getContentType(), file.getSize());
    }

    public record UploadResult(String key, String url, String originalName, String contentType, long size) {}
}
