package boot.data.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;
    private final long presignTtlMinutes;

    public S3StorageService(
            S3Client s3,
            S3Presigner presigner,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${app.s3.presign-ttl-minutes:30}") long presignTtlMinutes
    ) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
        this.presignTtlMinutes = presignTtlMinutes;
    }

    /** 모듈별 업로드 (예: modules = "jobpostings", "profiles", "companies", "articles" 등) */
    public UploadResult upload(String module, boolean isPublic, MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String base = (isPublic ? "public" : "private"); // 필요하면 모두 private 권장
        String key = base + "/" + module + "/" + datePath + "/" + UUID.randomUUID() + "_" + original;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 업로드 직후 보기용 프리사인드 GET URL 발급
        String presignedUrl = presignGetUrl(key, Duration.ofMinutes(presignTtlMinutes));

        return new UploadResult(key, presignedUrl, original, file.getContentType(), file.getSize());
    }

    /** 특정 key에 대해 프리사인드 GET 재발급 */
    public String presignGetUrl(String key, Duration ttl) {
        GetObjectRequest get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build();
        return presigner.presignGetObject(req).url().toString();
    }

    public record UploadResult(String key, String url, String originalName, String contentType, long size) {}
}
