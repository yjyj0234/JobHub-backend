package boot.data.controller.resume;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ResumeSkillResponse;
import boot.data.security.CurrentUser;
import boot.data.service.ResumeSkillService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resumes/{resumeId}/skills")
@RequiredArgsConstructor
public class ResumeSkillController {

    private final ResumeSkillService resumeSkillService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<ResumeSkillResponse>> list(
            @PathVariable("resumeId") long resumeId
    ) {
        long uid = currentUser.idOrThrow();
        return ResponseEntity.ok(resumeSkillService.list(resumeId, uid));
    }

    @PostMapping
    public ResponseEntity<Long> link(
            @PathVariable("resumeId") long resumeId,
            @RequestParam("skillId") int skillId
    ) {
        long uid = currentUser.idOrThrow();
        long linkId = resumeSkillService.link(resumeId, skillId, uid);
        return ResponseEntity.ok(linkId);
    }

    @DeleteMapping("/{resumeSkillId}")
    public ResponseEntity<Void> unlink(
            @PathVariable("resumeId") long resumeId,
            @PathVariable("resumeSkillId") long resumeSkillId
    ) {
        long uid = currentUser.idOrThrow();
        resumeSkillService.unlink(resumeId, resumeSkillId, uid);
        return ResponseEntity.noContent().build();
    }
}
