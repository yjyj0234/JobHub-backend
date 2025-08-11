package boot.data.repository;

import boot.data.entity.JobCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategories, Integer> {
    List<JobCategories> findByLevel(Short level);
    List<JobCategories> findByParentId(Integer parentId);
    List<JobCategories> findByParentIsNull();

    List<JobCategories> findAllByOrderByIdAsc();
}