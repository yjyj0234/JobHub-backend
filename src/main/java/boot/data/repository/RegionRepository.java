package boot.data.repository;

import boot.data.entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Regions, Integer> {
    List<Regions> findByLevel(Short level);
    List<Regions> findByParentId(Integer parentId);
}