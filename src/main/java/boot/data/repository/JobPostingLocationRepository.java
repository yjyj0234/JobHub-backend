// src/main/java/boot/data/repository/JobPostingLocationRepository.java
package boot.data.repository;

import boot.data.entity.JobPostingLocations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;  // ⬅️ 추가
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingLocationRepository extends JpaRepository<JobPostingLocations, Long> {

    /**
     * 특정 공고의 모든 지역 조회 (기본)
     */
    List<JobPostingLocations> findByJobPostingId(Long jobPostingId);

    /**
     * 특정 지역의 'OPEN' 상태 채용공고 수 카운트
     */
    @Query("""
        select count(jpl)
        from JobPostingLocations jpl
        where jpl.region.id = :regionId
          and jpl.jobPosting.status = 'OPEN'
    """)
    Long countActiveJobsByRegion(@Param("regionId") Integer regionId); // ⬅️ @Param 추가

    /**
     * 공고 상세에서 사용할 지역 목록 (부모 지역까지 fetch)
     * - region과 region.parent를 함께 가져와서 N+1 방지
     * - 중복 방지를 위해 distinct
     * - 대표 지역 우선 정렬
     */
    @Query("""
        select distinct jpl
        from JobPostingLocations jpl
        join fetch jpl.region r
        left join fetch r.parent rp
        where jpl.jobPosting.id = :jobId
        order by jpl.isPrimary desc, jpl.id asc
    """)
    List<JobPostingLocations> findByJobIdWithRegion(@Param("jobId") Long jobId); // ⬅️ @Param 추가
}
