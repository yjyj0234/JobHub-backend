package boot.data.repository;

import java.util.List;
import java.util.Optional;

// ✅ 올바른 import로 수정
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import boot.data.entity.JobPostingLikes;
import boot.data.entity.JobPostings;

@Repository
public interface JobPostingLikesRepository extends JpaRepository<JobPostingLikes, Long> {
    
    // ========== 유저별 좋아요 통계 ==========
    
    // 유저가 좋아요한 공고 수
    Long countByUserId(Long userId);
    
    // 유저가 좋아요한 모든 공고
    List<JobPostingLikes> findByUserId(Long userId);

    // 특정 좋아요 존재 여부
    boolean existsByUserIdAndJobPostingId(Long userId, Long jobPostingId);
    
    // 특정 좋아요 조회 (LikeService)
    Optional<JobPostingLikes> findByUserIdAndJobPostingId(Long userId, Long jobPostingId);
    
    // 사용자별 좋아요 목록 (최신순)
    List<JobPostingLikes> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // ========== 공고별 좋아요 통계 ==========
    
    // 특정 공고의 좋아요 수
    Long countByJobPostingId(Long jobPostingId);
    
    // ========== 추천 알고리즘용 ==========
    
    // 유저가 좋아요한 공고들의 카테고리 ID 목록
    @Query("SELECT DISTINCT jpc.jobCategory.id FROM JobPostingLikes jpl JOIN jpl.jobPosting jp JOIN jp.jobPostingCategories jpc WHERE jpl.user.id = :userId AND jpc.jobCategory.parent IS NOT NULL ORDER BY jpl.createdAt DESC")
    List<Integer> findLikedCategoryIdsByUserId(@Param("userId") Long userId);

    // 상위 카테고리 ID 목록 (추천 시스템용)
    @Query("SELECT DISTINCT jpc.jobCategory.parent.id FROM JobPostingLikes jpl JOIN jpl.jobPosting jp JOIN jp.jobPostingCategories jpc WHERE jpl.user.id = :userId AND jpc.jobCategory.parent IS NOT NULL ORDER BY jpl.createdAt DESC")
    List<Integer> findLikedTopCategoryIdsByUserId(@Param("userId") Long userId);
    
    //RAND() 함수 제거하고 Service에서 Collections.shuffle() 사용
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.jobPostingCategories jpc WHERE jpc.jobCategory.id IN :categoryIds AND jp.status = 'OPEN' AND NOT EXISTS (SELECT 1 FROM JobPostingLikes jpl WHERE jpl.jobPosting = jp AND jpl.user.id = :userId)")
    List<JobPostings> findRecommendedJobsByCategoryIds(@Param("categoryIds") List<Integer> categoryIds, @Param("userId") Long userId, Pageable pageable);
}