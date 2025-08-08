package boot.data.repository;

import java.util.List;
import java.util.Optional;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import boot.data.entity.JobPostings;
//채용 공고 Repository ,  JpaSpecificationExecutor 추가로 동적 쿼리 지원
public interface JobPostingSearchRepository extends JpaRepository<JobPostings,Long>,JpaSpecificationExecutor<JobPostings> {
    //채용공고 상세조회 (연관 엔티티 한번에 조회)
    //N+1 문제 해결을 위한 Fetch Join 사용

    @Query("select distinct jp from JobPostings jp left join fetch jp.company c left join fetch c.companyDetails cd left join fetch c.industry left join fetch c.companySize where jp.id = :id and jp.status = 'OPEN' ")
    Optional<JobPostings> findByIdWithDetails(@Param("id") Long id);

    //키워드 검색(제목,회사명,통합검색필드) , 대소문자 구분없이 검색 가능
    @Query("SELECT jp FROM JobPostings jp JOIN jp.company c WHERE jp.status = 'OPEN' AND (LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(jp.searchText) LIKE LOWER(CONCAT('%', :keyword, '%')))")
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

    

}
