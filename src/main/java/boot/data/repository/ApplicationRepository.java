package boot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import boot.data.entity.Applications;

public interface ApplicationRepository extends JpaRepository<Applications, Long>{

    boolean existsByUser_IdAndJobPosting_Id(Long userId, Long postingId);
    List<Applications> findByUser_IdOrderByAppliedAtDesc(Long userId);

    
    // ✅ 지원자 + 유저 + 이력서 한 번에 페치 (N+1 / lazy 문제 예방)
    @Query("""
        select a
        from Applications a
        join fetch a.user u
        left join fetch a.resume r
        where a.jobPosting.id = :postingId
        order by a.appliedAt desc
    """)
    List<Applications> findAllWithUserAndResumeByPostingId(@Param("postingId") Long postingId);

}
