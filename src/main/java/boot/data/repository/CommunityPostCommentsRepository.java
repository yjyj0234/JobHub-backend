package boot.data.repository;




import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import boot.data.entity.CommunityPostsComments;

@Repository
public interface CommunityPostCommentsRepository extends JpaRepository<CommunityPostsComments, Long> {

    List<CommunityPostsComments> findByPost_IdOrderByCreatedAtAsc(Long postId);

    // 작성자 본인의 댓글만 원자적으로 삭제 (단일 쿼리)
    Long deleteByIdAndUser_Id(Long id, Long userId);
}