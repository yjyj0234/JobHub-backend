// src/main/java/boot/data/controller/CompanyManagementController.java
package boot.data.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import boot.data.dto.CompanyProfileDto;
import boot.data.dto.CompanyProfileRequest;
import boot.data.service.CompanyManagementService;
import jakarta.validation.Valid;

import java.util.List;
import boot.data.entity.Industry;
import boot.data.entity.CompanySize;
import boot.data.repository.IndustryRepository;
import boot.data.repository.CompanySizeRepository;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyManagementController {
    
    private final CompanyManagementService companyManagementService;
    private final IndustryRepository industryRepository;
    private final CompanySizeRepository companySizeRepository;
    
    /**
     * 내 기업 정보 조회
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('COMPANY','ADMIN')")
    public ResponseEntity<CompanyProfileDto> getMyCompanyProfile() {
        try {
            CompanyProfileDto profile = companyManagementService.getMyCompanyProfile();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("기업 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 기업 정보 저장 (생성/수정)
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('COMPANY','ADMIN')")
    public ResponseEntity<?> saveCompanyProfile(
            @Valid @RequestBody CompanyProfileRequest request) {
        try {
            CompanyProfileDto saved = companyManagementService.saveCompanyProfile(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("기업 정보 저장 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }
    
    /**
     * 기업 정보 존재 여부 확인
     */
    @GetMapping("/profile/exists")
    @PreAuthorize("hasRole('COMPANY','ADMIN')")
    public ResponseEntity<?> hasCompanyProfile() {
        try {
            boolean exists = companyManagementService.hasCompanyProfile();
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            log.error("기업 정보 확인 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }
    
    /**
     * 기업 정보 삭제
     */
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('COMPANY','ADMIN')")
    public ResponseEntity<?> deleteCompanyProfile() {
        try {
            companyManagementService.deleteCompanyProfile();
            return ResponseEntity.ok(Map.of("message", "기업 정보가 삭제되었습니다"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("기업 정보 삭제 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다"));
        }
    }
    
    /**
     * 업종 목록 조회
     */
    @GetMapping("/industries")
    public ResponseEntity<List<Industry>> getIndustries() {
        List<Industry> industries = industryRepository.findAll();
        return ResponseEntity.ok(industries);
    }
    
    /**
     * 기업 규모 목록 조회
     */
    @GetMapping("/company-sizes")
    public ResponseEntity<List<CompanySize>> getCompanySizes() {
        List<CompanySize> sizes = companySizeRepository.findAll();
        return ResponseEntity.ok(sizes);
    }
}