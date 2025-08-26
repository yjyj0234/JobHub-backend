// boot/data/repository/resume/ResumeCertificationsRepository.java
package boot.data.repository.resume;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.ResumeCertifications;

public interface ResumeCertificationsRepository extends JpaRepository<ResumeCertifications, Long> {

    List<ResumeCertifications> findByResume_IdOrderByIdDesc(Long resumeId);

    Optional<ResumeCertifications> findByIdAndResume_Id(Long id, Long resumeId);
}
