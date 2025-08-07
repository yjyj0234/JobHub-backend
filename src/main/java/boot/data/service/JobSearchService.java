// src/main/java/boot/data/service/JobSearchService.java
package boot.data.service;

import boot.data.dto.*;
import boot.data.entity.*;
import boot.data.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채용공고 검색 서비스
 * 검색 조건별 최적화된 쿼리 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSearchService {
    
    private final JobPostingRepository jobPostingRepository;
    private final SearchKeywordRepository searchKeywordRepository;
    
    /**
     * 통합 검색 메서드
     * 검색 조건에 따라 적절한 Repository 메서드 호출
     */
    public Page<JobSearchResponseDto> search(JobSearchRequestDto request) {
        log.info("검색 요청: keyword={}, regions={}, categories={}", 
                request.getKeyword(), request.getRegionIds(), request.getCategoryIds());
        
        // 페이징 설정 (정렬 기준 포함)
        PageRequest pageRequest = PageRequest.of(
            request.getPage(), 
            request.getSize(),
            Sort.by(Sort.Direction.DESC, getSortField(request.getSortBy()))
        );
        
        Page<JobPostings> result;
        
        // 검색 조건에 따른 분기
        if (hasOnlyKeyword(request)) {
            // 키워드만 있는 경우
            result = jobPostingRepository.searchByKeyword(request.getKeyword(), pageRequest);
            // 검색 키워드 통계 업데이트 (비동기)
            updateSearchKeyword(request.getKeyword());
        } else if (hasFilters(request)) {
            // 필터 조건이 있는 경우
            result = jobPostingRepository.searchWithFilters(
                request.getRegionIds(),
                request.getCategoryIds(),
                pageRequest
            );
        } else {
            // 조건 없는 경우 (전체 조회)
            result = jobPostingRepository.findAll(pageRequest);
        }
        
        // Entity -> DTO 변환
        return result.map(this::convertToDto);
    }
    
    /**
     * Entity를 DTO로 변환
     * 필요한 정보만 선택적으로 포함
     */
    private JobSearchResponseDto convertToDto(JobPostings entity) {
        return JobSearchResponseDto.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .companyId(entity.getCompany().getId())
            .companyName(entity.getCompany().getName())
            .companyLogo(entity.getCompany().getCompanyDetails() != null ? 
                        entity.getCompany().getCompanyDetails().getLogoUrl() : null)
            .regions(entity.getJobPostingLocations().stream()
                    .map(jpl -> jpl.getRegion().getName())
                    .collect(Collectors.toList()))
            .categories(entity.getJobPostingCategories().stream()
                    .map(jpc -> jpc.getJobCategory().getName())
                    .collect(Collectors.toList()))
            .viewCount(entity.getViewCount())
            .applicationCount(entity.getApplicationCount())
            .closeDate(entity.getCloseDate())
            .closeType(entity.getCloseType().name())
            .isRemote(entity.isRemote())
            .createdAt(entity.getCreatedAt())
            .build();
    }
    
    /**
     * 인기 검색어 조회 (캐시 적용)
     * 1시간마다 갱신
     */
    @Cacheable(value = "popularKeywords", key = "'top10'")
    public List<String> getPopularKeywords() {
        return searchKeywordRepository.findTop10ByOrderBySearchCountDesc()
                .stream()
                .map(SearchKeywords::getKeyword)
                .collect(Collectors.toList());
    }
    
    /**
     * 검색 키워드 통계 업데이트
     * @Async로 비동기 처리 가능
     */
    @Transactional
    public void updateSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        
        searchKeywordRepository.findByKeyword(keyword)
            .ifPresentOrElse(
                SearchKeywords::incrementSearchCount,
                () -> {
                    SearchKeywords newKeyword = new SearchKeywords();
                    newKeyword.setKeyword(keyword);
                    newKeyword.incrementSearchCount();
                    searchKeywordRepository.save(newKeyword);
                }
            );
    }
    
    // === 헬퍼 메서드 ===
    
    private boolean hasOnlyKeyword(JobSearchRequestDto request) {
        return request.getKeyword() != null && 
                (request.getRegionIds() == null || request.getRegionIds().isEmpty()) &&
                (request.getCategoryIds() == null || request.getCategoryIds().isEmpty());
    }
    
    private boolean hasFilters(JobSearchRequestDto request) {
        return (request.getRegionIds() != null && !request.getRegionIds().isEmpty()) ||
                (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty());
    }
    
    private String getSortField(String sortBy) {
        return switch(sortBy) {
            case "viewCount" -> "viewCount";
            case "applicationCount" -> "applicationCount";
            case "closeDate" -> "closeDate";
            default -> "createdAt";
        };
    }
}