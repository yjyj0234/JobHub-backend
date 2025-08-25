package boot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.Applications;

public interface ApplicationRepository extends JpaRepository<Applications, Long>{

    boolean existsByUser_IdAndJobPosting_Id(Long userId, Long postingId);
    List<Applications> findByUser_IdOrderByAppliedAtDesc(Long userId);
    
}
