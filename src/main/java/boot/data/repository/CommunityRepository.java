package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import boot.data.entity.CommunityPosts;

@Service
public interface CommunityRepository extends JpaRepository<CommunityPosts, Long>{
   
}
