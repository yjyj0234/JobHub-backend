// src/main/java/boot/data/repository/ResumeLanguagesRepository.java
package boot.data.repository.resume;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeLanguages;

public interface ResumeLanguagesRepository extends JpaRepository<ResumeLanguages, Long> {
    List<ResumeLanguages> findByResumeIdOrderByIdAsc(Long resumeId);
    boolean existsByIdAndResumeId(Long id, Long resumeId);
    void deleteByIdAndResumeId(Long id, Long resumeId);
}
