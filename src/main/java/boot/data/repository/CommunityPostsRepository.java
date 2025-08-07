package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import boot.data.entity.CommunityPosts;

@Repository
public interface CommunityPostsRepository extends JpaRepository<CommunityPosts, Long>{
   
}
