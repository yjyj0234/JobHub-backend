package boot.data.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import boot.data.entity.CommunityPostsComments;

@Repository
public interface CommunityPostCommentsRepository extends JpaRepository<CommunityPostsComments, Long>{

     // 게시글의 "삭제 안 된" 댓글을 생성일 오름차순으로
   List<CommunityPostsComments> findByPost_IdOrderByCreatedAtAsc(Long postId);

   
}
