// src/main/java/boot/data/controller/ResumePortfolioController.java
package boot.data.controller.resume;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ResumePortfolioDto;
import boot.data.service.ResumePortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class ResumePortfolioController {

    private final ResumePortfolioService service;

    /** 목록 */
    @GetMapping("/api/resumes/{resumeId}/portfolios")
    public ResponseEntity<List<ResumePortfolioDto>> list(
            @PathVariable("resumeId") Long resumeId) {
        return ResponseEntity.ok(service.list(resumeId));
    }

    /** 생성 */
    @PostMapping("/api/resumes/{resumeId}/portfolios")
    public ResponseEntity<ResumePortfolioDto> create(
            @PathVariable("resumeId") Long resumeId,
            @Valid @RequestBody ResumePortfolioDto request) {
        ResumePortfolioDto created = service.create(resumeId, request);
        return ResponseEntity.created(URI.create(
                "/api/resumes/" + resumeId + "/portfolios/" + created.getId()
        )).body(created);
    }

    /** 수정 (프론트 매핑: /api/resumes/portfolios/{id}) */
    @PutMapping("/api/resumes/portfolios/{id}")
    public ResponseEntity<ResumePortfolioDto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ResumePortfolioDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /** 삭제 (프론트 매핑: /api/resumes/portfolios/{id}) */
    @DeleteMapping("/api/resumes/portfolios/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
