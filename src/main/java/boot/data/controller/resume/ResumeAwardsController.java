// src/main/java/boot/data/controller/resume/ResumeAwardsController.java
package boot.data.controller.resume;

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

import boot.data.dto.resume.ResumeAwardRequest;
import boot.data.dto.resume.ResumeAwardResponse;
import boot.data.service.ResumeAwardsService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resumes/{resumeId}/awards")
public class ResumeAwardsController {

    private final ResumeAwardsService awardsService;

    public ResumeAwardsController(ResumeAwardsService awardsService) {
        this.awardsService = awardsService;
    }

    @GetMapping
    public List<ResumeAwardResponse> list(@PathVariable("resumeId") Long resumeId) {
        return awardsService.listByResumeId(resumeId);
    }

    @PostMapping
    public ResponseEntity<Long> create(
            @PathVariable("resumeId") Long resumeId,
            @Valid @RequestBody ResumeAwardRequest.Create dto
    ) {
        Long id = awardsService.create(resumeId, dto);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/{awardId}")
    public ResponseEntity<Void> update(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("awardId") Long awardId,
            @Valid @RequestBody ResumeAwardRequest.Update dto
    ) {
        awardsService.update(resumeId, awardId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{awardId}")
    public ResponseEntity<Void> delete(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("awardId") Long awardId
    ) {
        awardsService.delete(resumeId, awardId);
        return ResponseEntity.noContent().build();
    }
}
