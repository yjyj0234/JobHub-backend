package boot.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import boot.data.service.S3StorageService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@RestController
// ✅ 상위 경로를 /api/media 로 통일(아래 메서드들에 /api를 또 붙이지 않도록)
@RequestMapping("/api")
// ✅ CORS: 개발용 포트 5173/3000 둘 다 허용, 쿠키/자격증명 허용
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
public class UploadController {

    private final S3StorageService storage;

    // =========================
    // 1) 단일 파일 업로드 (에디터에서 주로 사용)
    // =========================
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Map<String, Object> uploadOne(@RequestParam("file") MultipartFile file,
                                     @RequestParam(name="module", defaultValue="articles") String module,
                                     @RequestParam(name="public", defaultValue="false") boolean isPublic) throws IOException {
    var r = storage.upload(module, isPublic, file);
    String viewerUrl = "/api/files/view?key=" + URLEncoder.encode(r.key(), StandardCharsets.UTF_8);
    return Map.of(
        "key", r.key(),
        "url", r.url(),              // (만료) 프리사인드
        "viewerUrl", viewerUrl,      // ✔ 고정 경로
        "originalName", r.originalName(),
        "contentType", r.contentType(),
        "size", r.size()
    );
}

    // =========================
    // 2) 다중 파일 업로드
    // =========================
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadMany(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(name = "module", defaultValue = "articles") String module,
            @RequestParam(name = "public", defaultValue = "false") boolean isPublic
    ) throws IOException {

        List<Map<String, Object>> out = new ArrayList<>();
        for (MultipartFile f : files) {
            var r = storage.upload(module, isPublic, f);
            out.add(Map.of(
                "key", r.key(),
                "url", r.url(),
                "viewerUrl", "/api/files/view?key=" + URLEncoder.encode(r.key(), StandardCharsets.UTF_8),
                "originalName", r.originalName(),
                "contentType", r.contentType(),
                "size", r.size()
              ));
        }
        return Map.of("files", out);
    }

    // =========================
    // 3) 프리사인드 GET 재발급
    // =========================
    // ⚠️ 기존 코드의 "/api/files/presign" 은 클래스 레벨 "/api"와 겹쳐서 "/api/api/..." 가 됨.
    //    아래처럼 "/presign" 으로 두고, 상위 @RequestMapping("/api/media")와 합쳐 "/api/media/presign" 이 최종 경로가 되게 한다.
    @GetMapping("/files/presign")
    public Map<String, Object> presign(
            @RequestParam String key,
            @RequestParam(name = "minutes", defaultValue = "30") long minutes
    ) {
        // TODO: 로그인/권한/소유권 검사(필요 시)
        String url = storage.presignGetUrl(key, Duration.ofMinutes(minutes));
        return Map.of("key", key, "url", url, "expiresInSec", minutes * 60);
    }

    // =========================
    // 4) 공통 에러 핸들러(개발 중 원인 파악용)
    // =========================
    @ExceptionHandler(Exception.class)
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> onError(Exception e) {
        e.printStackTrace();
        return Map.of(
                "error", e.getClass().getSimpleName(),
                "message", String.valueOf(e.getMessage())
        );
    }

    @GetMapping("/files/view")
    public org.springframework.http.ResponseEntity<Void> view(@RequestParam String key) {
        String url = storage.presignGetUrl(key, java.time.Duration.ofMinutes(15));
        return org.springframework.http.ResponseEntity.status(302)
                .location(java.net.URI.create(url))
                .build();
}

}
