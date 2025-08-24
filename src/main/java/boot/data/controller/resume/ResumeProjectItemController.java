package boot.data.controller.resume;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ResumeProjectRequest;
import boot.data.security.CurrentUser;
import boot.data.service.ResumeProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resumes/projects")
@RequiredArgsConstructor
@Validated
public class ResumeProjectItemController {

    private final ResumeProjectService resumeProjectService;
    private final CurrentUser currentUser;

    /* 수정 */
    @PutMapping("/{projectId}")
    public ResponseEntity<Void> update(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ResumeProjectRequest body
    ) {
        Long uid = currentUser.idOrThrow();
        resumeProjectService.update(projectId, body, uid);
        return ResponseEntity.noContent().build();
    }

    /* 삭제 */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(@PathVariable("projectId") Long projectId) {
        Long uid = currentUser.idOrThrow();
        resumeProjectService.delete(projectId, uid);
        return ResponseEntity.noContent().build();
    }
}
