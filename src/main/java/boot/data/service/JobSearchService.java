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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private final JobPostingSearchRepository jobPostingSearchRepository;
    private final SearchKeywordRepository searchKeywordRepository;
    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;

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
            result = jobPostingSearchRepository.searchByKeyword(request.getKeyword(), pageRequest);
            // 검색 키워드 통계 업데이트 (비동기)
            updateSearchKeyword(request.getKeyword());
        } else if (hasFilters(request)) {
            // 필터 조건이 있는 경우
            result = jobPostingSearchRepository.searchWithFilters(
                request.getRegionIds(),
                request.getCategoryIds(),
                pageRequest
            );
        } else {
            // 조건 없는 경우 (전체 조회)
            result = jobPostingSearchRepository.findAll(pageRequest);
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

    /**
     * 시/도 조회 (level 1)
     */
    public List<RegionDto> getTopLevelRegions() {
        return regionRepository.findByLevel((short) 1)
            .stream()
            .map(this::convertToRegionDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 시/군/구 조회 (하위 지역)
     */
    public List<RegionDto> getSubRegions(Integer parentId) {
        return regionRepository.findByParentId(parentId)
            .stream()
            .map(this::convertToRegionDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 직무 대분류 조회 (level 1)
     */
    public List<JobCategoryDto> getTopLevelCategories() {
        return jobCategoryRepository.findByLevel((short) 1)
            .stream()
            .map(this::convertToCategoryDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 직무 소분류 조회
     */
    public List<JobCategoryDto> getSubCategories(Integer parentId) {
        return jobCategoryRepository.findByParentId(parentId)
            .stream()
            .map(this::convertToCategoryDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Region Entity → DTO 변환
     */
    private RegionDto convertToRegionDto(Regions region) {
        return RegionDto.builder()
            .id(region.getId())
            .name(region.getName())
            .level(region.getLevel())
            .parentId(region.getParent() != null ? region.getParent().getId() : null)
            .build();
    }
    
    /**
     * JobCategory Entity → DTO 변환
     */
    private JobCategoryDto convertToCategoryDto(JobCategories category) {
        return JobCategoryDto.builder()
            .id(category.getId())
            .name(category.getName())
            .level(category.getLevel())
            .parentId(category.getParent() != null ? category.getParent().getId() : null)
            .build();
    }
    
     /**
     * ⭐ 지역 트리 구조 조회 (새로 추가)
     */
    public List<RegionTreeDto> getRegionTree() {
        // 1. 모든 지역 조회
        List<Regions> allRegions = regionRepository.findAll();
        
        // 2. Map으로 변환
        Map<Integer, RegionTreeDto> regionMap = new HashMap<>();
        Map<Integer, List<RegionTreeDto>> childrenMap = new HashMap<>();
        
        // 3. DTO 변환
        for (Regions region : allRegions) {
            RegionTreeDto dto = RegionTreeDto.builder()
                .id(region.getId())
                .name(region.getName())
                .level(region.getLevel())
                .parentId(region.getParent() != null ? region.getParent().getId() : null)
                .children(new ArrayList<>())
                .build();
            
            regionMap.put(region.getId(), dto);
            
            // 부모별로 자식 그룹화
            if (region.getParent() != null) {
                childrenMap.computeIfAbsent(region.getParent().getId(), k -> new ArrayList<>()).add(dto);
            }
        }
        
        // 4. 트리 구조 생성
        List<RegionTreeDto> rootRegions = new ArrayList<>();
        for (RegionTreeDto region : regionMap.values()) {
            if (region.getParentId() == null) {
                // 최상위 지역 (시/도)
                if (childrenMap.containsKey(region.getId())) {
                    region.setChildren(childrenMap.get(region.getId()));
                }
                rootRegions.add(region);
            }
        }
        
        // 5. 정렬
        rootRegions.sort(Comparator.comparing(RegionTreeDto::getId));
        rootRegions.forEach(r -> r.getChildren().sort(Comparator.comparing(RegionTreeDto::getId)));
        
        return rootRegions;
    }
    
    /**
     * ⭐ 직무 트리 구조 조회 (새로 추가)
     */
    public List<JobCategoryTreeDto> getJobCategoryTree() {
        // 1. 모든 직무 카테고리 조회
        List<JobCategories> allCategories = jobCategoryRepository.findAll();
        
        // 2. Map으로 변환
        Map<Integer, JobCategoryTreeDto> categoryMap = new HashMap<>();
        Map<Integer, List<JobCategoryTreeDto>> childrenMap = new HashMap<>();
        
        // 3. DTO 변환
        for (JobCategories category : allCategories) {
            JobCategoryTreeDto dto = JobCategoryTreeDto.builder()
                .id(category.getId())
                .name(category.getName())
                .level(category.getLevel())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(new ArrayList<>())
                .build();
            
            categoryMap.put(category.getId(), dto);
            
            if (category.getParent() != null) {
                childrenMap.computeIfAbsent(category.getParent().getId(), k -> new ArrayList<>()).add(dto);
            }
        }
        
        // 4. 트리 구조 생성
        List<JobCategoryTreeDto> rootCategories = new ArrayList<>();
        for (JobCategoryTreeDto category : categoryMap.values()) {
            if (category.getParentId() == null) {
                if (childrenMap.containsKey(category.getId())) {
                    category.setChildren(childrenMap.get(category.getId()));
                }
                rootCategories.add(category);
            }
        }
        
        // 5. 정렬
        rootCategories.sort(Comparator.comparing(JobCategoryTreeDto::getId));
        rootCategories.forEach(c -> c.getChildren().sort(Comparator.comparing(JobCategoryTreeDto::getId)));
        
        return rootCategories;
    }
}
