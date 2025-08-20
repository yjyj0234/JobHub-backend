package boot.data.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import boot.data.entity.Faqs;

public interface FaqRepository extends JpaRepository<Faqs, Long> {
    
    List<Faqs> findAllByOrderByIdAsc();
}