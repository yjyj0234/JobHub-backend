package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import boot.data.entity.JobPostingCategories;

public interface JobPostingCategoriesRepository extends JpaRepository<JobPostingCategories, Long> {}