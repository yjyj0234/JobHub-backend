package boot.data.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import boot.data.service.S3StorageService;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final S3StorageService storage;

    @CrossOrigin(origins = {"http://localhost:3000"}) // 개발용 CORS
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        for (MultipartFile f : files) {
            var r = storage.upload(f);
            out.add(Map.of(
                    "id", r.key(),
                    "url", r.url(),
                    "originalName", r.originalName(),
                    "contentType", r.contentType(),
                    "size", r.size()
            ));
        }
        return Map.of("files", out);
    }
    //에러 원인 찾을때
    @ExceptionHandler(Exception.class)
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> onError(Exception e) {
        e.printStackTrace();
        
        return Map.of("error", e.getClass().getSimpleName(),
        "message",String.valueOf(e.getMessage()));
    }

    // 페이지 열릴 때 key로 이 API를 호출해서 항상 새 url을 받아 <img src={url}>로 쓰면 됨
    @GetMapping("/api/files/presign")
    public Map<String, Object> presign(@RequestParam String key) {
        // TODO: 로그인/소유권 체크
        String url = storage.presignGetUrl(key, java.time.Duration.ofMinutes(30));
        return Map.of("key", key, "url", url, "expiresInSec", 30*60);
    }
}
