package boot.data.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.CompanyPostingListDto;
import boot.data.dto.JobPostingCreateDto;
import boot.data.dto.JobPostingUpdateDto;
import boot.data.service.CompanyPostingService;
import boot.data.type.PostingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/company/postings")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class CompanyPostingController {

    private final CompanyPostingService companyPostingService;

    /**
     * 내 회사의 공고 목록 조회
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<List<CompanyPostingListDto>> getMyPostings() {
        try {
            List<CompanyPostingListDto> postings = companyPostingService.getMyCompanyPostings();
            return ResponseEntity.ok(postings);
        } catch (Exception e) {
            log.error("공고 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 공고 단건 조회 (수정용)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<JobPostingCreateDto> getPosting(@PathVariable Long id) {
        try {
            JobPostingCreateDto posting = companyPostingService.getPostingForEdit(id);
            return ResponseEntity.ok(posting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("공고 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 공고 상태 변경 (DRAFT → OPEN, OPEN → CLOSED 등)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            PostingStatus newStatus = PostingStatus.valueOf(statusStr.toUpperCase());
            
            companyPostingService.updatePostingStatus(id, newStatus);
            return ResponseEntity.ok(Map.of("message", "상태가 변경되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("상태 변경 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }

    /**
     * 공고 수정
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<?> updatePosting(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingUpdateDto dto) {
        try {
            companyPostingService.updatePosting(id, dto);
            return ResponseEntity.ok(Map.of("message", "수정되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("공고 수정 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }

    /**
     * 공고 삭제
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<?> deletePosting(@PathVariable Long id) {
        try {
            companyPostingService.deletePosting(id);
            return ResponseEntity.ok(Map.of("message", "삭제되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("공고 삭제 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }
}