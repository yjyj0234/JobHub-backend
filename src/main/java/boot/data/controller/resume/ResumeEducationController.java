// src/main/java/boot/data/controller/resume/ResumeEducationController.java
package boot.data.controller.resume;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.EducationRequest;
import boot.data.dto.resume.EducationResponse;
import boot.data.security.AuthUser;
import boot.data.service.ResumeEducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ResumeEducationController {

    private final ResumeEducationService educationService;

    @GetMapping("/resumes/{resumeId}/educations")
    public Page<EducationResponse> list(
            @PathVariable("resumeId") Long resumeId,
            @AuthenticationPrincipal AuthUser current,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return educationService.list(resumeId, current.id(), pageable);
    }

    @PostMapping("/resumes/{resumeId}/educations")
    @ResponseStatus(HttpStatus.CREATED)
    public EducationResponse create(
            @PathVariable("resumeId") Long resumeId,
            @AuthenticationPrincipal AuthUser current,
            @Valid @RequestBody EducationRequest req
    ) {
        return educationService.create(resumeId, current.id(), req);
    }

    @GetMapping("/resumes/{resumeId}/educations/{educationId}")
    public EducationResponse getOneNested(
            @PathVariable("resumeId") Long resumeId,
            @PathVariable("educationId") Long educationId,
            @AuthenticationPrincipal AuthUser current
    ) {
        return educationService.getOne(educationId, current.id());
    }

    @PutMapping({"/resumes/{resumeId}/educations/{educationId}", "/resumes/educations/{educationId}"})
    public EducationResponse update(
            @PathVariable(value = "resumeId", required = false) Long resumeId,
            @PathVariable("educationId") Long educationId,
            @AuthenticationPrincipal AuthUser current,
            @Valid @RequestBody EducationRequest req
    ) {
        return educationService.update(educationId, current.id(), req);
    }

    @DeleteMapping({"/resumes/{resumeId}/educations/{educationId}", "/resumes/educations/{educationId}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable(value = "resumeId", required = false) Long resumeId,
            @PathVariable("educationId") Long educationId,
            @AuthenticationPrincipal AuthUser current
    ) {
        educationService.delete(educationId, current.id());
    }
}
