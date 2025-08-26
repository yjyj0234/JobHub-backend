package boot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import boot.data.entity.Applications;

public interface ApplicationRepository extends JpaRepository<Applications, Long>{

    boolean existsByUser_IdAndJobPosting_Id(Long userId, Long postingId);
    List<Applications> findByUser_IdOrderByAppliedAtDesc(Long userId);

    
  
    @Query(value = """
        SELECT 
          a.id               AS id,
          a.posting_id       AS postingId,
          a.resume_id        AS resumeId,
          a.user_id          AS userId,
          a.status           AS status,
          a.applied_at       AS appliedAt,
          a.viewed_at        AS viewedAt,
          up.name            AS applicantName,
          u.email            AS applicantEmail,
          r.title            AS resumeTitle,
          (SELECT rp.url
             FROM resume_portfolios rp
            WHERE rp.resume_id = r.id
            ORDER BY rp.id DESC
            LIMIT 1)         AS resumePortfolioUrl
        FROM applications a
        JOIN users u               ON u.id = a.user_id
        LEFT JOIN user_profiles up ON up.user_id = u.id
        LEFT JOIN resumes r        ON r.id = a.resume_id
        WHERE a.posting_id = :postingId
        """, nativeQuery = true)
    List<ApplicationListRow> findListRowsByPostingId(@Param("postingId") Long postingId);
}
