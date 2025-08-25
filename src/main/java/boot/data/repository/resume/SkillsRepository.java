// src/main/java/boot/data/repository/SkillsRepository.java
package boot.data.repository.resume;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.Skills;

public interface SkillsRepository extends JpaRepository<Skills, Integer> {
    Optional<Skills> findByNameIgnoreCase(String name);
}
