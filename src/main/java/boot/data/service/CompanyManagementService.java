// src/main/java/boot/data/service/CompanyManagementService.java
package boot.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import boot.data.dto.CompanyProfileDto;
import boot.data.dto.CompanyProfileRequest;
import boot.data.entity.*;
import boot.data.repository.*;
import boot.data.security.CurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyManagementService {
    
    private final CompaniesRepository companiesRepository;
    private final UsersRepository usersRepository;
    private final IndustryRepository industryRepository;
    private final CompanySizeRepository companySizeRepository;
    private final CurrentUser currentUser;
    private final ObjectMapper objectMapper;
    
    /**
     * 현재 로그인한 기업회원의 기업 정보 조회
     */
    @Transactional(readOnly = true)
    public CompanyProfileDto getMyCompanyProfile() {
        Long userId = currentUser.idOrThrow();
        
        // 기업 정보가 없으면 빈 DTO 반환 (신규 등록용)
        return companiesRepository.findByOwnerIdWithDetails(userId)
            .map(CompanyProfileDto::from)
            .orElse(CompanyProfileDto.builder().build());
    }
    
    /**
     * 기업 정보 생성 또는 수정
     */
    @Transactional
    public CompanyProfileDto saveCompanyProfile(CompanyProfileRequest request) {
        Long userId = currentUser.idOrThrow();
        
        // 사용자 조회 및 권한 확인
        Users user = usersRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        if (user.getUserType() != boot.data.type.UserType.COMPANY) {
            throw new IllegalStateException("기업 회원만 기업 정보를 등록할 수 있습니다");
        }
        
        // 기존 기업 정보 조회 또는 신규 생성
        Companies company = companiesRepository.findByOwnerIdWithDetails(userId)
            .orElseGet(() -> {
                Companies newCompany = new Companies();
                newCompany.setOwner(user);
                newCompany.setVerified(false); // 신규 등록시 미인증 상태
                newCompany.setActiveJobCount(0);
                return newCompany;
            });
        
        // 사업자번호 중복 체크 (자신 제외)
        if (company.getId() != null) {
            // 기존 기업 수정시
            if (companiesRepository.existsByBusinessNumberExcludingId(
                    request.getBusinessNumber(), company.getId())) {
                throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다");  // 🔥 수정
            }
        } else {
            // 신규 등록시
            if (companiesRepository.existsByBusinessNumber(request.getBusinessNumber())) {
                throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다");  // 🔥 수정
            }
        }
        
        // 기본 정보 업데이트
        company.setName(request.getName());
        company.setBusinessNumber(request.getBusinessNumber());
        
        // 업종 설정
        if (request.getIndustryId() != null) {
            Industry industry = industryRepository.findById(request.getIndustryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 업종입니다"));  // 🔥 수정
            company.setIndustry(industry);
        }
        
        // 기업 규모 설정
        if (request.getCompanySizeId() != null) {
            CompanySize companySize = companySizeRepository.findById(request.getCompanySizeId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 기업 규모입니다"));  // 🔥 수정
            company.setCompanySize(companySize);
        }
        
        company.setFoundedYear(request.getFoundedYear());
        
        // 상세 정보 처리
        CompanyDetails details = company.getCompanyDetails();
        if (details == null) {
            details = new CompanyDetails();
            details.setCompany(company);
            company.setCompanyDetails(details);
        }
        
        // 상세 정보 업데이트
        details.setWebsiteUrl(request.getWebsiteUrl());
        details.setLogoUrl(request.getLogoUrl());
        details.setDescription(request.getDescription());
        details.setMission(request.getMission());
        details.setCulture(request.getCulture());
        
        // benefits 리스트를 JSON으로 변환
        try {
            if (request.getBenefits() != null && !request.getBenefits().isEmpty()) {
                String benefitsJson = objectMapper.writeValueAsString(request.getBenefits());
                details.setBenefits(benefitsJson);
            } else {
                details.setBenefits("[]");
            }
        } catch (Exception e) {
            log.error("Benefits JSON 변환 실패", e);
            details.setBenefits("[]");
        }
        
        // 저장
        Companies saved = companiesRepository.save(company);
        
        log.info("기업 정보 저장 완료 - companyId: {}, userId: {}, name: {}", 
                saved.getId(), userId, saved.getName());
        
        return CompanyProfileDto.from(saved);
    }
    
    /**
     * 기업 정보 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasCompanyProfile() {
        Long userId = currentUser.idOrThrow();
        return companiesRepository.existsByOwnerId(userId);
    }
    
    /**
     * 기업 정보 삭제 (필요시)
     */
    @Transactional
    public void deleteCompanyProfile() {
        Long userId = currentUser.idOrThrow();
        
        Companies company = companiesRepository.findByOwnerId(userId)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 기업 정보가 없습니다"));  // 🔥 수정
        
        // 채용공고가 있는 경우 삭제 불가
        if (company.getActiveJobCount() != null && company.getActiveJobCount() > 0) {
            throw new IllegalStateException("진행중인 채용공고가 있어 기업 정보를 삭제할 수 없습니다");  // 🔥 수정
        }
        
        companiesRepository.delete(company);
        log.info("기업 정보 삭제 완료 - companyId: {}, userId: {}", company.getId(), userId);
    }
}