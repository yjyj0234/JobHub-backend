package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.CompanyPostingListDto;
import boot.data.dto.JobPostingCreateDto;
import boot.data.dto.JobPostingUpdateDto;
import boot.data.entity.Companies;
import boot.data.entity.JobPostings;
import boot.data.repository.CompaniesRepository;
import boot.data.repository.JobPostingsRepository;
import boot.data.repository.JobPostingLocationRepository;
import boot.data.repository.JobPostingCategoriesRepository;
import boot.data.security.CurrentUser;
import boot.data.type.CloseType;
import boot.data.type.PostingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyPostingService {

    private final JobPostingsRepository jobPostingsRepository;
    private final CompaniesRepository companiesRepository;
    private final JobPostingLocationRepository locationRepository;
    private final JobPostingCategoriesRepository categoriesRepository;
    private final CurrentUser currentUser;

    /**
     * 내 회사의 모든 공고 조회
     */
    public List<CompanyPostingListDto> getMyCompanyPostings() {
        Long userId = currentUser.idOrThrow();
        
        // 내 회사 찾기
        Companies myCompany = companiesRepository.findByOwnerId(userId)
            .orElseThrow(() -> new IllegalStateException("회사 정보가 없습니다"));
        
        // 회사의 모든 공고 조회
        List<JobPostings> postings = jobPostingsRepository.findByCompanyIdOrderByCreatedAtDesc(myCompany.getId());
        
         for (JobPostings posting : postings) {
        checkAndUpdateExpiredStatus(posting);
    }
    
    return postings.stream()
        .map(this::toListDto)
        .collect(Collectors.toList());
    }

    /**
     * 공고 상태 변경
     */
    @Transactional
    public void updatePostingStatus(Long postingId, PostingStatus newStatus) {
        Long userId = currentUser.idOrThrow();
        
        JobPostings posting = jobPostingsRepository.findById(postingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));
        
        // 권한 체크
        if (!posting.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다");
        }
        
        // 상태 변경 검증
        validateStatusChange(posting.getStatus(), newStatus);
        
        posting.setStatus(newStatus);
        
        // OPEN으로 변경시 openDate 설정
        if (newStatus == PostingStatus.OPEN && posting.getOpenDate() == null) {
            posting.setOpenDate(LocalDateTime.now());
        }
        
        // CLOSED로 변경시 closeDate 설정
        if (newStatus == PostingStatus.CLOSED && posting.getCloseDate() == null) {
            posting.setCloseDate(LocalDateTime.now());
        }
        
        jobPostingsRepository.save(posting);
    }

    /**
     * 공고 삭제
     */
    @Transactional
    public void deletePosting(Long postingId) {
        Long userId = currentUser.idOrThrow();
        
        JobPostings posting = jobPostingsRepository.findById(postingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));
        
        // 권한 체크
        if (!posting.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("삭제 권한이 없습니다");
        }
        
        // OPEN 상태는 삭제 불가
        if (posting.getStatus() == PostingStatus.OPEN) {
            throw new IllegalStateException("진행중인 공고는 삭제할 수 없습니다. 먼저 마감 처리해주세요.");
        }
        
        jobPostingsRepository.delete(posting);
    }

    /**
     * 공고 수정용 데이터 조회
     */
    public JobPostingCreateDto getPostingForEdit(Long postingId) {
        Long userId = currentUser.idOrThrow();
        
        JobPostings posting = jobPostingsRepository.findById(postingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));
        
        // 권한 체크
        if (!posting.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("조회 권한이 없습니다");
        }
        
        // Entity → DTO 변환 (기존 데이터 재활용)
        return toEditDto(posting);
    }

    /**
     * 공고 수정
     */
    @Transactional
    public void updatePosting(Long postingId, JobPostingUpdateDto dto) {
        Long userId = currentUser.idOrThrow();
        
        JobPostings posting = jobPostingsRepository.findById(postingId)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));
        
        // 권한 체크
        if (!posting.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다");
        }
        
        // 기본 정보 업데이트
        posting.setTitle(dto.getTitle());
        posting.setDescription(dto.getDescription());
        posting.setRemote(dto.isRemote());
        posting.setCloseType(dto.getCloseType());
        
        // TODO: 지역, 카테고리, 조건 업데이트 로직 추가
        
        posting.setUpdatedAt(LocalDateTime.now());
        jobPostingsRepository.save(posting);
    }

    /**
     * 상태 변경 검증
     */
    private void validateStatusChange(PostingStatus current, PostingStatus target) {
        // DRAFT → OPEN, CLOSED 가능
        // OPEN → CLOSED, EXPIRED 가능
        // CLOSED, EXPIRED → 변경 불가
        
        if (current == PostingStatus.CLOSED || current == PostingStatus.EXPIRED) {
            throw new IllegalStateException("마감/만료된 공고는 상태를 변경할 수 없습니다");
        }
        
        if (current == PostingStatus.DRAFT && target == PostingStatus.EXPIRED) {
            throw new IllegalStateException("임시저장 상태에서 바로 만료로 변경할 수 없습니다");
        }
    }

    /**
     * Entity → List DTO 변환
     */
    private CompanyPostingListDto toListDto(JobPostings posting) {
        // 지역 정보 조회
        List<String> locations = locationRepository.findByJobPostingId(posting.getId())
            .stream()
            .map(loc -> loc.getRegion().getName())
            .collect(Collectors.toList());
        
        // employmentType 처리
        String employmentType = null;
        if (posting.getJobPostingConditions() != null
            && posting.getJobPostingConditions().getEmploymentType() != null) {
            employmentType = posting.getJobPostingConditions()
                .getEmploymentType()
                .name(); // enum을 String으로 변환
        }
        
        return CompanyPostingListDto.builder()
            .id(posting.getId())
            .title(posting.getTitle())
            .status(posting.getStatus().name())
            .viewCount(posting.getViewCount())
            .applicationCount(posting.getApplicationCount())
            .createdAt(posting.getCreatedAt().toString())
            .closeDate(posting.getCloseDate() != null ? posting.getCloseDate().toString() : null)
            .locations(locations)
            .employmentType(employmentType) // String 타입으로 설정
            .build();
    }

    /**
     * Entity → Edit DTO 변환
     */
    private JobPostingCreateDto toEditDto(JobPostings posting) {
        // TODO: 실제 변환 로직 구현
        JobPostingCreateDto dto = new JobPostingCreateDto();
        dto.setTitle(posting.getTitle());
        dto.setDescription(posting.getDescription());
        dto.setStatus(posting.getStatus());
        dto.setCloseType(posting.getCloseType());
        // ... 나머지 필드 매핑
        return dto;
    }

    @Transactional
private void checkAndUpdateExpiredStatus(JobPostings posting) {
    // OPEN 상태이고, DEADLINE 타입이며, 마감일이 지난 경우
    if (posting.getStatus() == PostingStatus.OPEN 
        && posting.getCloseType() == CloseType.DEADLINE
        && posting.getCloseDate() != null
        && posting.getCloseDate().isBefore(LocalDateTime.now())) {
        
        posting.setStatus(PostingStatus.EXPIRED);
        jobPostingsRepository.save(posting);
        log.info("공고 ID {} 상태를 EXPIRED로 변경", posting.getId());
    }
}

    
}