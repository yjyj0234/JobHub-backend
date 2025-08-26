// src/main/java/boot/data/repository/resume/ResumeAwardsRepository.java
package boot.data.repository.resume;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeAwards;

public interface ResumeAwardsRepository extends JpaRepository<ResumeAwards, Long> {
    List<ResumeAwards> findAllByResume_IdOrderByIdDesc(Long resumeId);
    Optional<ResumeAwards> findByIdAndResume_Id(Long id, Long resumeId);
    boolean existsByIdAndResume_User_Id(Long id, Long userId);
}
