package boot.data.repository;

import boot.data.entity.JobPostingConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingConditionsRepository extends JpaRepository<JobPostingConditions, Long> {
    // JobPostingConditions 엔티티에 대한 CRUD 기능을 자동으로 제공합니다.
}
