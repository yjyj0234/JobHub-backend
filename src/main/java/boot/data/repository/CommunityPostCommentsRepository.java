package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import boot.data.entity.CommunityPostsComments;

@Repository
public interface CommunityPostCommentsRepository extends JpaRepository<CommunityPostsComments, Long>{

}
