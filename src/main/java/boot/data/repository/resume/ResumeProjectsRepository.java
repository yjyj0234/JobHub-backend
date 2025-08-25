package boot.data.repository.resume;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeProjects;

public interface ResumeProjectsRepository extends JpaRepository<ResumeProjects, Long> {

    // 이력서 ID로 목록
    List<ResumeProjects> findByResume_IdOrderByIdDesc(Long resumeId);

    // 단건 + 이력서 소유자 검증용 (projectId + ownerId)
    Optional<ResumeProjects> findByIdAndResume_User_Id(Long id, Long userId);

    // 단건 + 이력서 ID 검증용 (list 시 사용 X, 방어코드 용도)
    Optional<ResumeProjects> findByIdAndResume_Id(Long id, Long resumeId);
}
