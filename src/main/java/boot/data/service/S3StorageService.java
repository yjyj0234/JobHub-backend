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

    // yml에서 조정 가능하게(기본 30분)
    private final long presignTtlMinutes;

    public S3StorageService(
            S3Client s3,
            S3Presigner presigner,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            // 없으면 30분 디폴트
            @Value("${app.s3.presign-ttl-minutes:30}") long presignTtlMinutes
    ) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
        this.presignTtlMinutes = presignTtlMinutes;
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

        // ✅ 업로드 직후 30분짜리 프리사인드 GET URL 생성
        String presignedUrl = presignGetUrl(key, Duration.ofMinutes(presignTtlMinutes));

        // 반환: key는 DB 보관용, url은 미리보기용(만료됨)
        return new UploadResult(key, presignedUrl, original, file.getContentType(), file.getSize());
    }

    /** 필요 시 재발급용 메서드 (컨트롤러에서 호출해 새 티켓 발급) */
    public String presignGetUrl(String key, Duration ttl) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build();

        return presigner.presignGetObject(req).url().toString();
    }
  
    public record UploadResult(String key, String url, String originalName, String contentType, long size) {}
}
