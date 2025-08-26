
package boot.data.repository.resume;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeExperiences;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperiences, Long> {
    List<ResumeExperiences> findAllByResume_IdOrderByIdDesc(Long resumeId);
}
