package boot.data.repository;




import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import boot.data.entity.CommunityPostsComments;

@Repository
public interface CommunityPostCommentsRepository extends JpaRepository<CommunityPostsComments, Long> {

    List<CommunityPostsComments> findByPost_IdOrderByCreatedAtDesc(Long postId);

    // 작성자 본인의 댓글만 원자적으로 삭제 (단일 쿼리)
    Long deleteByIdAndUser_Id(Long id, Long userId);

    // 댓글 수 카운트
    Long countByPost_Id(Long postid); // 게시글 ID로 조회하여 존재 여부 확인
     @Query("select c.post.id as postId, count(c) as cnt " +
           "from CommunityPostsComments c " +
           "where c.post.id in :postIds " +
           "group by c.post.id")
    List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);

     //댓글 삭제 시 게시글의 댓글도 같이 삭제
    @Modifying
    @Transactional
    void deleteByPost_Id(Long postId);

    // 여러 게시글별 삭제되지 않은 댓글 수 집계
    @Query("select c.post.id as postId, count(c) as cnt " +
           "from CommunityPostsComments c " +
           "where c.post.id in :postIds and (c.content is not null and c.content <> '') " +
           "group by c.post.id")
    List<Object[]> countByPostId(@Param("postIds") List<Long> postIds);

    
}
