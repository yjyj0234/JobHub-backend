package boot.data.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import boot.data.dto.CompanyDetailDto;
import boot.data.dto.JobPostingSimpleDto;
import boot.data.service.CompanyPublicService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/public/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class CompanyController {
    
    private final CompanyPublicService companyPublicService;
    
    /**
     * 기업 상세 정보 조회 (공개)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDetailDto> getCompanyDetail(@PathVariable Long id) {
        try {
            CompanyDetailDto company = companyPublicService.getCompanyDetail(id);
            return ResponseEntity.ok(company);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 기업의 진행중인 채용공고 목록
     */
    @GetMapping("/{id}/postings")
    public ResponseEntity<List<JobPostingSimpleDto>> getCompanyPostings(@PathVariable Long id) {
        try {
            List<JobPostingSimpleDto> postings = companyPublicService.getActivePostings(id);
            return ResponseEntity.ok(postings);
        } catch (Exception e) {
            log.error("채용공고 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}