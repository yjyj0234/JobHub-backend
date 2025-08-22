
package boot.data.repository.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.Resumes;

public interface ResumesRepository extends JpaRepository<Resumes, Long> {
}
