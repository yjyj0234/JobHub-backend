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
}
