package boot.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import boot.data.entity.Resumes;

@Repository
public interface ResumeRepository extends JpaRepository<Resumes, Long> {
    
    //특정 사용자의 모든 이력서 조회 (최신순으로 정렬)
    @Query("select r from Resumes r where r.user.id = :userId order by r.id desc")
    List<Resumes> findByUserIdOrderByIdDesc(@Param("userId") Long userId);

    /**
     * 특정 사용자의 대표 이력서 조회
     * @param userId 사용자 ID
     * @return 대표 이력서 (없으면 Optional.empty())
     */
    @Query("SELECT r FROM Resumes r WHERE r.user.id = :userId AND r.isPrimary = true")
    Optional<Resumes> findPrimaryResumeByUserId(@Param("userId") Long userId);
    
    /**
     * 이력서 ID와 소유자 ID로 조회 (권한 체크용)
     * @param resumeId 이력서 ID
     * @param userId 사용자 ID
     * @return 해당 사용자의 해당 이력서 (권한 없으면 Optional.empty())
     */
    @Query("SELECT r FROM Resumes r WHERE r.id = :resumeId AND r.user.id = :userId")
    Optional<Resumes> findByIdAndUserId(@Param("resumeId") Long resumeId, @Param("userId") Long userId);
    
    /**
     * 특정 사용자의 모든 이력서를 비대표로 변경
     * 새로운 대표 이력서 설정 전에 기존 대표 해제용
     * @param userId 사용자 ID
     */
    @Modifying
    @Query("UPDATE Resumes r SET r.isPrimary = false WHERE r.user.id = :userId AND r.isPrimary = true")
    void clearPrimaryStatus(@Param("userId") Long userId);
    
    /**
     * 특정 이력서를 대표 이력서로 설정
     * @param resumeId 이력서 ID
     */
    @Modifying
    @Query("UPDATE Resumes r SET r.isPrimary = true WHERE r.id = :resumeId")
    void setPrimaryStatus(@Param("resumeId") Long resumeId);
    
    /**
     * 이력서 완성도 업데이트
     * @param resumeId 이력서 ID
     * @param completionRate 완성도 (0-100)
     */
    @Modifying
    @Query("UPDATE Resumes r SET r.completionRate = :completionRate WHERE r.id = :resumeId")
    void updateCompletionRate(@Param("resumeId") Long resumeId, @Param("completionRate") Short completionRate);
    
    /**
     * 공개된 이력서만 조회 (기업회원이 구직자 검색용)
     * @return 공개된 이력서 목록
     */
    @Query("SELECT r FROM Resumes r WHERE r.isPublic = true ORDER BY r.id DESC")
    List<Resumes> findPublicResumes();
    
    /**
     * 사용자 이력서 수 카운트
     * @param userId 사용자 ID
     * @return 이력서 개수
     */
    @Query("SELECT COUNT(r) FROM Resumes r WHERE r.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);


    

}
