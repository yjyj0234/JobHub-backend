package boot.data.service;

import boot.data.dto.*;
import boot.data.entity.Companies;
import boot.data.entity.JobPostings;
import boot.data.repository.*;
import boot.data.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {
    
    private final JobPostingSearchRepository jobPostingSearchRepository;
    private final JobPostingLikesRepository jobPostingLikesRepository;
    private final CompanyBookmarksRepository companyBookmarksRepository;
    private final CurrentUser currentUser;
    
    /**
     * 개인화된 채용공고 추천 (좋아요 기반)
     */
    public List<RecommendedJobDto> getPersonalizedRecommendations(int limit) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return getPopularJobPostings(limit);
            }
            
            // 사용자가 좋아요한 직무 대분류
            List<Integer> likedCategories = jobPostingLikesRepository.findLikedTopCategoryIdsByUserId(userId);
            
            if (likedCategories.isEmpty()) {
                return getPopularJobPostings(limit);
            }
            
            // 좋아요 카테고리 기반 추천 (이미 좋아요한 공고 제외)
            List<JobPostings> recommendations = jobPostingSearchRepository.findRecommendedJobsByUserLikes(likedCategories, userId, PageRequest.of(0, limit * 2));
            
            // 랜덤 셔플 후 제한
            Collections.shuffle(recommendations);
            List<JobPostings> limitedRecommendations = recommendations.stream()
                    .limit(limit)
                    .toList();
            
            return convertToRecommendedJobDto(limitedRecommendations, userId);
            
        } catch (Exception e) {
            log.warn("개인화 추천 실패, 기본 인기 공고 반환", e);
            return getPopularJobPostings(limit);
        }
    }
    
    /**
     * 인기 기업 TOP 10 (뷰카운트 기준) - ✅ 수정
     */
    public List<CompanyRankingDto> getTopCompaniesByViewCount() {
        List<Object[]> topCompanies = jobPostingSearchRepository.findTopCompaniesByViewCount();
        
        return IntStream.range(0, topCompanies.size())
                .mapToObj(index -> {
                    Object[] row = topCompanies.get(index);
                    return CompanyRankingDto.builder()
                            .companyId((Long) row[0])
                            .companyName((String) row[1])
                            .companyLogo((String) row[2])
                            .industry((String) row[4])
                            .bookmarkCount(((Number) row[5]).longValue()) // 여기서는 뷰카운트
                            .ranking(index + 1)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 북마크 수 기준 인기 기업 TOP 10 - ✅ 수정
     */
    public List<CompanyRankingDto> getTopCompaniesByBookmarkCount() {
        List<Object[]> topCompanies = companyBookmarksRepository.findTop10CompaniesByBookmarkCount(PageRequest.of(0, 10));
        
        return IntStream.range(0, topCompanies.size())
                .mapToObj(index -> {
                    Object[] row = topCompanies.get(index);
                    Companies company = (Companies) row[0];
                    return CompanyRankingDto.builder()
                            .companyId(company.getId())
                            .companyName(company.getName())
                            .companyLogo(getCompanyLogo(company))
                            .industry(getCompanyIndustry(company))
                            .bookmarkCount(((Number) row[1]).longValue())
                            .ranking(index + 1)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    
    private List<RecommendedJobDto> getPopularJobPostings(int limit) {
        List<JobPostings> popularJobs = jobPostingSearchRepository
                .findTopByViewCount(PageRequest.of(0, limit));
        
        Long userId = getCurrentUserId();
        return convertToRecommendedJobDto(popularJobs, userId);
    }
    
    private List<RecommendedJobDto> convertToRecommendedJobDto(List<JobPostings> jobPostings, Long userId) {
        List<Long> userLikes = userId != null ? 
                getUserLikedJobIds(userId) : new ArrayList<>();
        
        return jobPostings.stream()
                .map(job -> RecommendedJobDto.builder()
                        .jobPostingId(job.getId())
                        .title(job.getTitle())
                        .companyName(job.getCompany().getName())
                        .companyLogo(getCompanyLogo(job.getCompany()))
                        .categories(extractJobCategories(job))
                        .regions(extractRegions(job))
                        .closeDate(job.getCloseDate())
                        .viewCount(job.getViewCount())
                        .isLiked(userLikes.contains(job.getId()))
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<Long> getUserLikedJobIds(Long userId) {
        return jobPostingLikesRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(like -> like.getJobPosting().getId())
                .toList();
    }
    
    private Long getCurrentUserId() {
        return currentUser.get()
                .map(auth -> auth.id())
                .orElse(null);
    }
    
    // Helper methods
    private String getCompanyLogo(Companies company) {
        return company.getCompanyDetails() != null ? 
                company.getCompanyDetails().getLogoUrl() : null;
    }
    
    private String getCompanyIndustry(Companies company) {
        return company.getIndustry() != null ? 
                company.getIndustry().getName() : "";
    }
    
    private List<String> extractJobCategories(JobPostings job) {
        return job.getJobPostingCategories().stream()
                .map(jpc -> jpc.getJobCategory().getName())
                .collect(Collectors.toList());
    }
    
    private List<String> extractRegions(JobPostings job) {
        return job.getJobPostingLocations().stream()
                .map(jpl -> jpl.getRegion().getName())
                .collect(Collectors.toList());
    }
}