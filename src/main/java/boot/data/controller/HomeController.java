package boot.data.controller;

import boot.data.dto.*;
import boot.data.service.RecommendationService;
import boot.data.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class HomeController {
    
    private final RecommendationService recommendationService;
    private final LikeService likeService;
    
    /**
     * 홈페이지 추천 채용공고
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @RequestParam(defaultValue = "12") int limit) {
        List<RecommendedJobDto> recommendations =
                recommendationService.getPersonalizedRecommendations(limit);
        
        return ResponseEntity.ok(Map.of("recommendations", recommendations));
    }
    
    /**
     * 인기 기업 TOP 10 (뷰카운트 기준)
     */
    @GetMapping("/top-companies")
    public ResponseEntity<?> getTopCompanies() {
        List<CompanyRankingDto> topCompanies = recommendationService.getTopCompaniesByViewCount();
        
        return ResponseEntity.ok(Map.of("companies", topCompanies));
    }
    
    /**
     * 인기 기업 TOP 10 (북마크 기준)
     */
    @GetMapping("/top-companies-bookmark")
    public ResponseEntity<?> getTopCompaniesByBookmark() {
        List<CompanyRankingDto> topCompanies = recommendationService.getTopCompaniesByBookmarkCount();
        
        return ResponseEntity.ok(Map.of("companies", topCompanies));
    }
    
    /**
     * 채용공고 좋아요 토글
     */
    @PostMapping("/likes/{jobPostingId}")
    @PreAuthorize("hasAnyRole('USER', 'COMPANY')")
    public ResponseEntity<?> toggleLike(@PathVariable Long jobPostingId) {
        boolean isLiked = likeService.toggleLike(jobPostingId);
        
        return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "message", isLiked ? "좋아요가 추가되었습니다" : "좋아요가 제거되었습니다"
        ));
    }
}