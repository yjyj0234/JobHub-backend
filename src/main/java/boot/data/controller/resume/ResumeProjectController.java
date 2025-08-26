package boot.data.controller.resume;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ResumeProjectRequest;
import boot.data.dto.resume.ResumeProjectResponse;
import boot.data.security.CurrentUser;
import boot.data.service.ResumeProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resumes/{resumeId}/projects")
@RequiredArgsConstructor
@Validated
public class ResumeProjectController {

    private final ResumeProjectService resumeProjectService;
    private final CurrentUser currentUser;

    /* 목록 */
    @GetMapping
    public ResponseEntity<List<ResumeProjectResponse>> list(
            @PathVariable("resumeId") Long resumeId
    ) {
        Long uid = currentUser.idOrThrow();
        return ResponseEntity.ok(resumeProjectService.list(resumeId, uid));
    }

    /* 생성 */
    @PostMapping
    public ResponseEntity<Long> create(
            @PathVariable("resumeId") Long resumeId,
            @Valid @RequestBody ResumeProjectRequest body
    ) {
        Long uid = currentUser.idOrThrow();
        Long id = resumeProjectService.create(resumeId, body, uid);
        return ResponseEntity.ok(id);
    }
}
