// src/main/java/boot/data/repository/ResumePortfoliosRepository.java
package boot.data.repository.resume;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumePortfolios;

public interface ResumePortfoliosRepository extends JpaRepository<ResumePortfolios, Long> {
    List<ResumePortfolios> findByResumeIdOrderByIdAsc(Long resumeId);
    boolean existsByIdAndResumeId(Long id, Long resumeId);
    void deleteByIdAndResumeId(Long id, Long resumeId);
}
