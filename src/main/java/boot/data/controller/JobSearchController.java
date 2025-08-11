// src/main/java/boot/data/controller/JobSearchController.java
package boot.data.controller;

import boot.data.dto.*;
import boot.data.service.JobSearchService;  // ⭐ JobSearchService만 사용
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class JobSearchController {
    
    private final JobSearchService jobSearchService;  // ⭐ CategoryService 제거
    
    /**
     * 채용공고 검색
     */
    @PostMapping("/jobs")
    public ResponseEntity<Page<JobSearchResponseDto>> searchJobs(
        @RequestBody JobSearchRequestDto request) {
        log.info("검색 요청: {}", request);
        return ResponseEntity.ok(jobSearchService.search(request));
    }
    
    /**
     * 지역 카테고리 조회
     */
    @GetMapping("/regions")
    public ResponseEntity<?> getRegions(
            @RequestParam(value = "parentId", required = false) Integer parentId) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (parentId == null) {
            // 최상위 지역 (시/도)
            List<RegionDto> regions = jobSearchService.getTopLevelRegions();  // ⭐ jobSearchService 사용
            response.put("regions", regions);
            response.put("level", 1);
        } else {
            // 하위 지역 (시/군/구)
            List<RegionDto> subRegions = jobSearchService.getSubRegions(parentId);  // ⭐ jobSearchService 사용
            response.put("regions", subRegions);
            response.put("parentId", parentId);
            response.put("level", 2);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 직무 카테고리 조회
     */
    @GetMapping("/job-categories")
    public ResponseEntity<?> getJobCategories(
            @RequestParam(value = "parentId",required = false) Integer parentId) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (parentId == null) {
            // 대분류
            List<JobCategoryDto> categories = jobSearchService.getTopLevelCategories();  // ⭐ jobSearchService 사용
            response.put("categories", categories);
        } else {
            // 소분류
            List<JobCategoryDto> subCategories = jobSearchService.getSubCategories(parentId);  // ⭐ jobSearchService 사용
            response.put("categories", subCategories);
            response.put("parentId", parentId);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 인기 검색어
     */
    @GetMapping("/popular-keywords")
    public ResponseEntity<?> getPopularKeywords() {
        List<String> keywords = jobSearchService.getPopularKeywords();
        return ResponseEntity.ok(Map.of("keywords", keywords));
    }
    // JobSearchController.java에 추가
    @GetMapping("/regions/tree")
    public ResponseEntity<?> getRegionTree() {
    List<RegionTreeDto> regionTree = jobSearchService.getRegionTree();
    return ResponseEntity.ok(Map.of("regions", regionTree));
}

    @GetMapping("/job-categories/tree")
    public ResponseEntity<?> getJobCategoryTree() {
    List<JobCategoryTreeDto> categoryTree = jobSearchService.getJobCategoryTree();
    return ResponseEntity.ok(Map.of("categories", categoryTree));
}
}