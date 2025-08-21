// src/main/java/boot/data/repository/resume/ResumeEducationRepository.java
package boot.data.repository.resume;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeEducations;

public interface ResumeEducationRepository extends JpaRepository<ResumeEducations, Long> {

    // 특정 이력서 + 소유자 기준 목록 (보안)
    Page<ResumeEducations> findByResume_IdAndResume_User_Id(Long resumeId, Long userId, Pageable pageable);

    // 단건도 소유자 기준으로 조회 (보안)
    Optional<ResumeEducations> findByIdAndResume_User_Id(Long id, Long userId);
}
