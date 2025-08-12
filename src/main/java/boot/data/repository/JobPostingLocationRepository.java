// 공고조회 Repository
package boot.data.repository;

import boot.data.entity.JobPostingLocations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JobPostingLocationRepository extends JpaRepository<JobPostingLocations, Long> {
    
    /**
     * 특정 공고의 모든 지역 조회
     */
    List<JobPostingLocations> findByJobPostingId(Long jobPostingId);
    
    /**
     * 특정 지역의 채용공고 수 카운트
     */
    @Query("SELECT COUNT(jpl) FROM JobPostingLocations jpl " +
            "WHERE jpl.region.id = :regionId " +
            "AND jpl.jobPosting.status = 'OPEN'")
    Long countActiveJobsByRegion(Integer regionId);
}