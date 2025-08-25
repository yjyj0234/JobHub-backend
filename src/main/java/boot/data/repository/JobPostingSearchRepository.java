package boot.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import boot.data.entity.JobPostings;

//채용 공고 Repository ,  JpaSpecificationExecutor 추가로 동적 쿼리 지원
@Repository
public interface JobPostingSearchRepository extends JpaRepository<JobPostings,Long>,JpaSpecificationExecutor<JobPostings> {
    //채용공고 상세조회 (연관 엔티티 한번에 조회)
    //N+1 문제 해결을 위한 Fetch Join 사용

    @Query("select distinct jp from JobPostings jp left join fetch jp.company c left join fetch c.companyDetails cd left join fetch c.industry left join fetch c.companySize where jp.id = :id and jp.status = 'OPEN' ")
    Optional<JobPostings> findByIdWithDetails(@Param("id") Long id);

    //키워드 검색(제목,회사명,통합검색필드) , 대소문자 구분없이 검색 가능
    @Query("SELECT DISTINCT jp FROM JobPostings jp WHERE jp.status = 'OPEN' AND (jp.title LIKE %:keyword% OR jp.company.name LIKE %:keyword%)")
    Page<JobPostings> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    //복합검색 (지역+직무 카테고리)
    //EXISTS 사용으로 중복 제거
    @Query("SELECT DISTINCT jp FROM JobPostings jp WHERE jp.status = 'OPEN' AND (:regionIds IS NULL OR EXISTS (SELECT 1 FROM JobPostingLocations jpl WHERE jpl.jobPosting = jp AND jpl.region.id IN :regionIds)) AND (:categoryIds IS NULL OR EXISTS (SELECT 1 FROM JobPostingCategories jpc WHERE jpc.jobPosting = jp AND jpc.jobCategory.id IN :categoryIds))")
    Page<JobPostings> searchWithFilters(@Param("regionIds") List<Integer> regionIds, @Param("categoryIds") List<Integer> categoryIds, Pageable pageable);

    //조회수 증가 (벌크 업데이트)
    //@Modifying 으로 update 쿼리 실행
    @Modifying
    @Query("UPDATE JobPostings jp SET jp.viewCount = jp.viewCount + 1 WHERE jp.id = :id")
    void incrementViewCount(@Param("id") Long id);

    //회사별 채용 공고
    @Query("SELECT jp FROM JobPostings jp WHERE jp.company.id = :companyId AND jp.status = 'OPEN' ORDER BY jp.createdAt DESC")
    List<JobPostings> findByCompanyId(@Param("companyId") Long companyId);

    //조회수 기준으로 한 인기채용공고
    @Query("SELECT jp FROM JobPostings jp WHERE jp.status = 'OPEN' ORDER BY jp.viewCount DESC")
    List<JobPostings> findTopByViewCount(Pageable pageable);

    // 지역만 검색
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.jobPostingLocations jpl WHERE jp.status = 'OPEN' AND jpl.region.id IN :regionIds")
    Page<JobPostings> searchByRegions(@Param("regionIds") List<Integer> regionIds, Pageable pageable);

    // 직무만 검색  
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.jobPostingCategories jpc WHERE jp.status = 'OPEN' AND jpc.jobCategory.id IN :categoryIds")
    Page<JobPostings> searchByCategories(@Param("categoryIds") List<Integer> categoryIds, Pageable pageable);

    // 키워드 + 지역
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.company c JOIN jp.jobPostingLocations jpl WHERE jp.status = 'OPEN' AND (LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND jpl.region.id IN :regionIds")
    Page<JobPostings> searchByKeywordAndRegions(@Param("keyword") String keyword, @Param("regionIds") List<Integer> regionIds, Pageable pageable);

    // 키워드 + 직무
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.company c JOIN jp.jobPostingCategories jpc WHERE jp.status = 'OPEN' AND (LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND jpc.jobCategory.id IN :categoryIds")
    Page<JobPostings> searchByKeywordAndCategories(@Param("keyword") String keyword, @Param("categoryIds") List<Integer> categoryIds, Pageable pageable);

    // 지역 + 직무
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.jobPostingLocations jpl JOIN jp.jobPostingCategories jpc WHERE jp.status = 'OPEN' AND jpl.region.id IN :regionIds AND jpc.jobCategory.id IN :categoryIds")
    Page<JobPostings> searchByRegionsAndCategories(@Param("regionIds") List<Integer> regionIds, @Param("categoryIds") List<Integer> categoryIds, Pageable pageable);
    
    // 좋아요한 카테고리 기반 추천 공고
    @Query("SELECT DISTINCT jp FROM JobPostings jp JOIN jp.jobPostingCategories jpc JOIN jpc.jobCategory jc WHERE jp.status = 'OPEN' AND (jc.id IN :categoryIds OR jc.parent.id IN :categoryIds) AND jp.id NOT IN (SELECT jpl.jobPosting.id FROM JobPostingLikes jpl WHERE jpl.user.id = :userId) ORDER BY jp.viewCount DESC, jp.createdAt DESC")
    List<JobPostings> findRecommendedJobsByUserLikes(@Param("categoryIds") List<Integer> categoryIds, @Param("userId") Long userId, Pageable pageable);

    // 뷰카운트 기준 TOP 기업
    @Query("SELECT c.id, c.name, cd.logoUrl, cd.description, i.name, SUM(jp.viewCount), COUNT(jp.id) FROM JobPostings jp JOIN jp.company c LEFT JOIN c.companyDetails cd LEFT JOIN c.industry i WHERE jp.status = 'OPEN' GROUP BY c.id, c.name, cd.logoUrl, cd.description, i.name ORDER BY SUM(jp.viewCount) DESC")
    List<Object[]> findTopCompaniesByViewCount(Pageable pageable);

    default List<Object[]> findTopCompaniesByViewCount() {
        return findTopCompaniesByViewCount(PageRequest.of(0, 10));
    }
}