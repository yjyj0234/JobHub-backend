// boot/data/controller/resume/ResumeCertificationController.java
package boot.data.controller.resume;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ResumeCertificationRequest;
import boot.data.dto.resume.ResumeCertificationResponse;
import boot.data.security.CurrentUser;
import boot.data.service.ResumeCertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes/{resumeId}/certifications")
@Validated
public class ResumeCertificationController {

    private final ResumeCertificationService certificationService;
    private final CurrentUser currentUser;

    private Long uid() { return currentUser.idOrThrow(); }

    // 목록 조회 200
    @GetMapping
    public ResponseEntity<List<ResumeCertificationResponse>> list(
            @PathVariable("resumeId") Long resumeId) {
        return ResponseEntity.ok(certificationService.list(resumeId, uid()));
    }

    // 생성 201 + Location
    @PostMapping
    public ResponseEntity<Long> create(
            @PathVariable("resumeId") Long resumeId,
            @Valid @RequestBody ResumeCertificationRequest.Create req) {
        Long id = certificationService.create(resumeId, req, uid());
        return ResponseEntity
                .created(URI.create("/api/resumes/" + resumeId + "/certifications/" + id))
                .body(id);
    }

    // 수정 204
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("id") Long id,
            @Valid @RequestBody ResumeCertificationRequest.Update req) {
        certificationService.update(resumeId, id, req, uid());
        return ResponseEntity.noContent().build();
    }

    // 삭제 204
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("id") Long id) {
        certificationService.delete(resumeId, id, uid());
    }
}
