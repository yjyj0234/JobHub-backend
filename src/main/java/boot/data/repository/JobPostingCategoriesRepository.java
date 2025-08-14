package boot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import boot.data.entity.JobPostingCategories;

public interface JobPostingCategoriesRepository extends JpaRepository<JobPostingCategories, Long> {

@Query("""
        select c
        from JobPostingCategories c
        join fetch c.jobCategory cat
        where c.jobPosting.id = :jobId
        order by c.isPrimary desc, c.id asc
    """)
    List<JobPostingCategories> findByJobIdWithCategory(Long jobId);

}