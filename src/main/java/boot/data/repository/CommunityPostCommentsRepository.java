package boot.data.repository;




import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import boot.data.entity.CommunityPostsComments;

@Repository
public interface CommunityPostCommentsRepository extends JpaRepository<CommunityPostsComments, Long> {

    List<CommunityPostsComments> findByPost_IdOrderByCreatedAtDesc(Long postId);

    // 작성자 본인의 댓글만 원자적으로 삭제 (단일 쿼리)
    Long deleteByIdAndUser_Id(Long id, Long userId);

    Long countByPost_Id(Long postid); // 게시글 ID로 조회하여 존재 여부 확인
     @Query("select c.post.id as postId, count(c) as cnt from CommunityPostsComments c where c.post.id in :postIds group by c.post.id")
    List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
