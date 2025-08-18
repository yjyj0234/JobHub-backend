// src/main/java/boot/data/controller/resume/ResumeActivityController.java
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

import boot.data.dto.resume.ActivityRequest;
import boot.data.dto.resume.ActivityResponse;
import boot.data.security.AuthUser;
import boot.data.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resumes/{resumeId}/activities")
public class ResumeActivityController {

    private final ResumeService resumeService;

    /** 목록 조회 */
    @GetMapping
    public Page<ActivityResponse> list(
        @PathVariable("resumeId") Long resumeId,
        @AuthenticationPrincipal AuthUser current,
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return resumeService.listActivities(resumeId, current.id(), pageable);
    }

    /** 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse create(
        @PathVariable("resumeId") Long resumeId,
        @AuthenticationPrincipal AuthUser current,
        @Valid @RequestBody ActivityRequest req
    ) {
        return resumeService.createActivity(resumeId, current.id(), req);
    }

    /** 단건 조회 */
    @GetMapping("/{activityId}")
    public ActivityResponse getOne(
        @PathVariable("resumeId") Long resumeId,
        @PathVariable("activityId") Long activityId,
        @AuthenticationPrincipal AuthUser current
    ) {
        return resumeService.getActivity(activityId, current.id());
    }

    /** 수정 */
    @PutMapping("/{activityId}")
    public ActivityResponse update(
        @PathVariable("resumeId") Long resumeId,
        @PathVariable("activityId") Long activityId,
        @AuthenticationPrincipal AuthUser current,
        @Valid @RequestBody ActivityRequest req
    ) {
        return resumeService.updateActivity(activityId, current.id(), req);
    }

    /** 삭제 */
    @DeleteMapping("/{activityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable("resumeId") Long resumeId,
        @PathVariable("activityId") Long activityId,
        @AuthenticationPrincipal AuthUser current
    ) {
        resumeService.deleteActivity(activityId, current.id());
    }
}
