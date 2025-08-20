// src/main/java/boot/data/repository/IndustryRepository.java
package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import boot.data.entity.Industry;
import java.util.List;

public interface IndustryRepository extends JpaRepository<Industry, Long> {
    List<Industry> findAllByOrderByNameAsc();  // 이름순 정렬 조회
}