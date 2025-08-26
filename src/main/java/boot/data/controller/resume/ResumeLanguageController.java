// src/main/java/boot/data/controller/ResumeLanguageController.java
package boot.data.controller.resume;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ResumeLanguageDto;
import boot.data.service.ResumeLanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes/{resumeId}/languages")
public class ResumeLanguageController {

    private final ResumeLanguageService service;

    @GetMapping
    public ResponseEntity<List<ResumeLanguageDto>> list(
            @PathVariable("resumeId") Long resumeId) {
        return ResponseEntity.ok(service.list(resumeId));
    }

    @PostMapping
    public ResponseEntity<ResumeLanguageDto> create(
            @PathVariable("resumeId") Long resumeId,
            @Valid @RequestBody ResumeLanguageDto request) {
        ResumeLanguageDto created = service.create(resumeId, request);
        return ResponseEntity.created(URI.create(
                "/api/resumes/" + resumeId + "/languages/" + created.getId()
        )).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeLanguageDto> update(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("id") Long id,
            @Valid @RequestBody ResumeLanguageDto request) {
        return ResponseEntity.ok(service.update(resumeId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("id") Long id) {
        service.delete(resumeId, id);
        return ResponseEntity.noContent().build();
    }
}
