// src/main/java/boot/data/service/CompanyPublicService.java
package boot.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import boot.data.dto.CompanyDetailDto;
import boot.data.dto.JobPostingSimpleDto;
import boot.data.entity.Companies;
import boot.data.entity.JobPostings;
import boot.data.repository.CompaniesRepository;
import boot.data.repository.JobPostingsRepository;
import boot.data.type.PostingStatus;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyPublicService {
    
    private final CompaniesRepository companiesRepository;
    private final JobPostingsRepository jobPostingsRepository;
    
    /**
     * 기업 상세 정보 조회 (공개)
     */
    @Transactional(readOnly = true)
    public CompanyDetailDto getCompanyDetail(Long companyId) {
        Companies company = companiesRepository.findByIdWithAll(companyId)
            .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다"));
        
        return CompanyDetailDto.from(company);
    }
    
    /**
     * 기업의 진행중인 채용공고
     */
    @Transactional(readOnly = true)
    public List<JobPostingSimpleDto> getActivePostings(Long companyId) {
        List<JobPostings> postings = jobPostingsRepository
            .findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, PostingStatus.OPEN);
        
        return postings.stream()
            .map(JobPostingSimpleDto::from)
            .collect(Collectors.toList());
    }
}