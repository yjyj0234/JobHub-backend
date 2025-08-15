// src/main/java/boot/data/controller/resume/ResumeController.java
package boot.data.controller.resume;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.resume.ResumeCreateDto;
import boot.data.dto.resume.ResumeResponse;
import boot.data.entity.Resumes;
import boot.data.security.AuthUser;
import boot.data.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    /** 이력서 생성 -> 생성된 ID 반환 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@AuthenticationPrincipal AuthUser current,
                       @Valid @RequestBody ResumeCreateDto dto) {
        // ✅ 반드시 로그인 사용자의 ID 사용 (dto.userId 사용 금지)
        return resumeService.createResume(current.id(), dto);
    }

    /** 내 이력서 목록 (최신순) */
    @GetMapping
    public List<ResumeResponse> myResumes(@AuthenticationPrincipal AuthUser current) {
        List<Resumes> list = resumeService.getResumesByUserId(current.id());
        return list.stream().map(ResumeResponse::from).toList();
    }

    /** 내 이력서 단건 조회 */
    @GetMapping("/{resumeId}")
    public ResumeResponse getOne(@PathVariable("resumeId") Long resumeId,
                                 @AuthenticationPrincipal AuthUser current) {
        Resumes r = resumeService.getResumeById(resumeId, current.id());
        return ResumeResponse.from(r);
    }

    /** 내 이력서 수정 */
    @PutMapping("/{resumeId}")
    public Long update(@PathVariable("resumeId") Long resumeId,
                       @AuthenticationPrincipal AuthUser current,
                       @Valid @RequestBody ResumeCreateDto dto) {
        return resumeService.updateResume(resumeId, current.id(), dto);
    }

    /** 내 이력서 삭제 */
    @DeleteMapping("/{resumeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("resumeId") Long resumeId,
                       @AuthenticationPrincipal AuthUser current) {
        resumeService.deleteResume(resumeId, current.id());
    }

    /** 대표 이력서 설정 */
    @PatchMapping("/{resumeId}/primary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPrimary(@PathVariable("resumeId") Long resumeId,
                           @AuthenticationPrincipal AuthUser current) {
        resumeService.setPrimaryResume(resumeId, current.id());
    }

    /** 공개 이력서 목록 (기업/비로그인 검색용) */
    @GetMapping("/public/list")
    public List<ResumeResponse> publicResumes() {
        return resumeService.getPublicResumes().stream()
                .map(ResumeResponse::from)
                .toList();
    }
}
