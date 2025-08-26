package boot.data.repository.resume;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import boot.data.entity.ResumeActivity;

/**
 * resume_activities CRUD용 레포지토리
 * - 항상 소유자(user_id)까지 함께 검증하는 메서드 제공
 * - LAZY 초기화 이슈 방지를 위해 resume 연관관계는 EntityGraph로 로딩
 */
@Repository
public interface ResumeActivityRepository extends JpaRepository<ResumeActivity, Long> {

    /** 목록 조회: 특정 이력서 + 해당 이력서가 현재 사용자 소유인지까지 필터 */
    @EntityGraph(attributePaths = "resume")
    Page<ResumeActivity> findByResume_IdAndResume_User_Id(Long resumeId, Long userId, Pageable pageable);

    /** 단건 조회(수정/삭제용): 소유권 검증 포함 */
    @EntityGraph(attributePaths = "resume")
    Optional<ResumeActivity> findByIdAndResume_User_Id(Long id, Long userId);

    /** 삭제(소유권 검증 포함) */
    long deleteByIdAndResume_User_Id(Long id, Long userId);

    /** 빠른 소유권 체크 */
    boolean existsByResume_IdAndResume_User_Id(Long resumeId, Long userId);

     /** 단건 조회(소유권 검증 포함): 이력서 ID와 사용자 ID로 조회 */
    @EntityGraph(attributePaths = "resume")
Optional<ResumeActivity> findByIdAndResume_IdAndResume_User_Id(Long id, Long resumeId, Long userId);

    
}
