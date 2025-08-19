package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import boot.data.entity.CommunityPosts;

@Repository
public interface CommunityPostsRepository extends JpaRepository<CommunityPosts, Long>{
   
    // 조회수 증가 (동시성 대응 / 벌크 업데이트)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommunityPosts p set p.viewCount = coalesce(p.viewCount, 0) + 1 where p.id = :id")
    int increaseViewCount(@Param("id") Long id);
}
