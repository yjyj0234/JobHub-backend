package boot.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.UserProfiles;

public interface UserProfilesRepository extends JpaRepository<UserProfiles, Long> {
    Optional<UserProfiles> findByUserId(Long userId);       // user_profiles.name 가져올 때 사용
}
