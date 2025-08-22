package boot.data.repository.resume;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeSkills;

public interface ResumeSkillsRepository extends JpaRepository<ResumeSkills, Long> {
    List<ResumeSkills> findByResume_Id(Long resumeId);
    boolean existsByResume_IdAndSkill_Id(Long resumeId, Integer skillId);
    Optional<ResumeSkills> findByIdAndResume_Id(Long id, Long resumeId);

    // 🔹 고아 스킬 정리용: 해당 skillId가 다른 이력서에 아직 연결돼있는지 카운트
    long countBySkill_Id(Integer skillId);

    // 🔹 권한 검사용(선택): 링크 id + 이력서 id + 사용자 id로 단건 조회
    Optional<ResumeSkills> findByIdAndResume_IdAndResume_User_Id(Long id, Long resumeId, Long userId);
}