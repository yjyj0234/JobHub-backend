// src/main/java/boot/data/service/JobSearchService.java
package boot.data.service;

import boot.data.dto.*;
import boot.data.entity.*;
import boot.data.repository.*;
import boot.data.type.CloseType;
import boot.data.type.PostingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        log.info("검색 요청 받음: {}", request);
        
        // ✅ NULL 체크를 맨 앞으로 이동 (PageRequest 생성 전에!)
        if (request.getPage() == null) {
            request.setPage(0);
        }
        if (request.getSize() == null) {
            request.setSize(20);
        }
        if (request.getSortBy() == null || request.getSortBy().isEmpty()) {
            request.setSortBy("latest");
        }
        if (request.getRegionIds() == null) {
            request.setRegionIds(new ArrayList<>());
        }
        if (request.getCategoryIds() == null) {
            request.setCategoryIds(new ArrayList<>());
        }
        
        log.info("정규화 후: page={}, size={}, sortBy={}", 
            request.getPage(), request.getSize(), request.getSortBy());
        
        // 이제 안전하게 PageRequest 생성
        PageRequest pageRequest = PageRequest.of(
            request.getPage(), 
            request.getSize(),
            Sort.by(Sort.Direction.DESC, getSortField(request.getSortBy()))
        );
        
        // 검색 조건 정규화
        String keyword = (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) 
                        ? request.getKeyword().trim() : null;
        List<Integer> regionIds = (!request.getRegionIds().isEmpty()) 
                                ? request.getRegionIds() : null;
        List<Integer> categoryIds = (!request.getCategoryIds().isEmpty()) 
                                   ? request.getCategoryIds() : null;
        
        log.info("검색 조건: keyword='{}', regionIds={}, categoryIds={}", 
            keyword, regionIds, categoryIds);
        
        Page<JobPostings> result;
        
        // 조건별 분기 처리
        if (keyword == null && regionIds == null && categoryIds == null) {
            // 전체 조회
            log.info("전체 조회");
            result = jobPostingSearchRepository.findAll(
                PageRequest.of(request.getPage(), request.getSize(), 
                Sort.by(Sort.Direction.DESC, "viewCount"))
            );
        } 
        else if (keyword != null && regionIds == null && categoryIds == null) {
            // 키워드만
            log.info("키워드만 검색: {}", keyword);
            result = jobPostingSearchRepository.searchByKeyword(keyword, pageRequest);
            //updateSearchKeyword(keyword);
        }
        else if (keyword == null && regionIds != null && categoryIds == null) {
            // 지역만
            log.info("지역만 검색: {}", regionIds);
            result = jobPostingSearchRepository.searchByRegions(regionIds, pageRequest);
        }
        else if (keyword == null && regionIds == null && categoryIds != null) {
            // 직무만
            log.info("직무만 검색: {}", categoryIds);
            result = jobPostingSearchRepository.searchByCategories(categoryIds, pageRequest);
        }
        else if (keyword != null && regionIds != null && categoryIds == null) {
            // 키워드 + 지역
            log.info("키워드+지역 검색: keyword={}, regions={}", keyword, regionIds);
            result = jobPostingSearchRepository.searchByKeywordAndRegions(keyword, regionIds, pageRequest);
            updateSearchKeyword(keyword);
        }
        else if (keyword != null && regionIds == null && categoryIds != null) {
            // 키워드 + 직무
            log.info("키워드+직무 검색: keyword={}, categories={}", keyword, categoryIds);
            result = jobPostingSearchRepository.searchByKeywordAndCategories(keyword, categoryIds, pageRequest);
            updateSearchKeyword(keyword);
        }
        else if (keyword == null && regionIds != null && categoryIds != null) {
            // 지역 + 직무
            log.info("지역+직무 검색: regions={}, categories={}", regionIds, categoryIds);
            result = jobPostingSearchRepository.searchByRegionsAndCategories(regionIds, categoryIds, pageRequest);
        }
        else {
            // 키워드 + 지역 + 직무 (전체 조합)
            log.info("전체 조합 검색: keyword={}, regions={}, categories={}", keyword, regionIds, categoryIds);
            // 임시로 키워드만 검색 처리 (나중에 개선)
            result = jobPostingSearchRepository.searchByKeyword(keyword, pageRequest);
            updateSearchKeyword(keyword);
        }
        
        // 마감일이 지난 공고 상태 업데이트
        result.getContent().forEach(this::checkAndUpdateExpiredStatus);
        
        log.info("검색 결과: {} 건", result.getTotalElements());

        List<JobPostings> filteredList = result.getContent().stream()
        .filter(posting -> posting.getStatus() != PostingStatus.DRAFT)
        .collect(Collectors.toList());
    
        Page<JobPostings> filteredResult = new PageImpl<>(
        filteredList, 
        result.getPageable(), 
        filteredList.size()
    );
    
        return result.map(this::convertToDto);
    }
    
    @Transactional
    private void checkAndUpdateExpiredStatus(JobPostings posting) {
    // OPEN 상태이고, DEADLINE 타입이며, 마감일이 지난 경우
    if (posting.getStatus() == PostingStatus.OPEN 
        && posting.getCloseType() == CloseType.DEADLINE
        && posting.getCloseDate() != null
        && posting.getCloseDate().isBefore(LocalDateTime.now())) {
        
        posting.setStatus(PostingStatus.EXPIRED);
        // Repository 주입이 필요하면 상단에 추가
        // jobPostingsRepository.save(posting);
        log.debug("공고 ID {} 상태를 EXPIRED로 변경", posting.getId());
    }
}
    /**
     * Entity를 DTO로 변환
     * 필요한 정보만 선택적으로 포함
     */
    private JobSearchResponseDto convertToDto(JobPostings entity) {
        // 지역 정보 안전하게 추출
        List<String> regions = new ArrayList<>();
        if (entity.getJobPostingLocations() != null) {
            regions = entity.getJobPostingLocations().stream()
                    .filter(jpl -> jpl.getRegion() != null)
                    .map(jpl -> jpl.getRegion().getName())
                    .collect(Collectors.toList());
        }
        
        // 직무 카테고리 정보 안전하게 추출
        List<String> categories = new ArrayList<>();
        if (entity.getJobPostingCategories() != null) {
            categories = entity.getJobPostingCategories().stream()
                    .filter(jpc -> jpc.getJobCategory() != null)
                    .map(jpc -> jpc.getJobCategory().getName())
                    .collect(Collectors.toList());
        }
        
        // 회사 로고 URL 안전하게 추출
        String companyLogo = null;
        if (entity.getCompany() != null && entity.getCompany().getCompanyDetails() != null) {
            companyLogo = entity.getCompany().getCompanyDetails().getLogoUrl();
        }
        
        // 회사명 안전하게 추출
        String companyName = "회사명 없음";
        if (entity.getCompany() != null && entity.getCompany().getName() != null) {
            companyName = entity.getCompany().getName();
        }
        // ✅ JobPostingConditions 데이터 추출
    String experienceLevel = null;
    String educationLevel = null;
    String employmentType = null;
    String salaryType = null;
    Integer minSalary = null;
    Integer maxSalary = null;
    Short minExperienceYears = null;
    Short maxExperienceYears = null;
    
    // JobPostingConditions 안전하게 접근
    if (entity.getJobPostingConditions() != null) {
        JobPostingConditions conditions = entity.getJobPostingConditions();
        
        experienceLevel = conditions.getExperienceLevel() != null ?
            conditions.getExperienceLevel().name() : null;
        educationLevel = conditions.getEducationLevel() != null ?
            conditions.getEducationLevel().name() : null;
        employmentType = conditions.getEmploymentType() != null ?
            conditions.getEmploymentType().name() : null;
        salaryType = conditions.getSalaryType() != null ?
            conditions.getSalaryType().name() : null;
        minSalary = conditions.getMinSalary();
        maxSalary = conditions.getMaxSalary();
        minExperienceYears = conditions.getMinExperienceYears();
        maxExperienceYears = conditions.getMaxExperienceYears();
    }
        
        return JobSearchResponseDto.builder()
            .id(entity.getId())
            .title(entity.getTitle() != null ? entity.getTitle() : "제목 없음")
            .companyId(entity.getCompany() != null ? entity.getCompany().getId() : null)
            .companyName(companyName)
            .companyLogo(companyLogo)
            .regions(regions)
            .categories(categories)
            .viewCount(entity.getViewCount() != null ? entity.getViewCount() : 0)
            .applicationCount(entity.getApplicationCount() != null ? entity.getApplicationCount() : 0)
            .closeDate(entity.getCloseDate())
            .closeType(entity.getCloseType() != null ? entity.getCloseType().name() : "")
            .status(entity.getStatus() != null ? entity.getStatus().name() : "OPEN")
            .isRemote(entity.isRemote())
            .createdAt(entity.getCreatedAt())
            .experienceLevel(experienceLevel)
            .educationLevel(educationLevel)
            .employmentType(employmentType)
            .salaryType(salaryType)
            .minSalary(minSalary)
            .maxSalary(maxSalary)
            .minExperienceYears(minExperienceYears)
            .maxExperienceYears(maxExperienceYears)
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
        
        try {
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
        } catch (Exception e) {
            log.warn("검색 키워드 저장 실패: {}", keyword, e);
        }
    }
    
    private String getSortField(String sortBy) {
        return switch(sortBy) {
            case "viewCount", "views" -> "viewCount";
            case "applicationCount", "applications" -> "applicationCount";
            case "closeDate", "deadline" -> "closeDate";
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