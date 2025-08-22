package boot.data.repository;

import boot.data.entity.GroupChatRooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupChatRoomsRepository extends JpaRepository<GroupChatRooms, Long> {

    // 내가 참여한 방만 최신순
    @Query("""
      select r from GroupChatRooms r
      where exists (select m.id from GroupChatMembers m
                    where m.room.id = r.id and m.user.id = :uid)
      order by r.id desc
    """)
    List<GroupChatRooms> findMyRooms(@Param("uid") Long uid);

    
    
}
