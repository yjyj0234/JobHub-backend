// src/main/java/boot/data/repository/CompanySizeRepository.java
package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import boot.data.entity.CompanySize;
import java.util.List;

public interface CompanySizeRepository extends JpaRepository<CompanySize, Integer> {
    List<CompanySize> findAllByOrderByMinEmployeesAsc();  // 규모순 정렬 조회
}