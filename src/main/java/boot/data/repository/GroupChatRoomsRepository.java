package boot.data.repository;

import boot.data.entity.GroupChatRooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GroupChatRoomsRepository extends JpaRepository<GroupChatRooms, Long> {

    // 내가 참여한 방만 최신순
    @Query("""
      select r from GroupChatRooms r
      where exists (select m.id from GroupChatMembers m
                    where m.room.id = r.id and m.user.id = :uid)
      order by r.id desc
    """)
    List<GroupChatRooms> findMyRooms(@Param("uid") Long uid);

     // ✅ 두 사용자 사이의 1:1 방 조회(이미 수락된 방; 멤버수=2)
    @Query("""
        select r
        from GroupChatRooms r
        where exists (select m1.id from GroupChatMembers m1 where m1.room.id = r.id and m1.user.id = :a)
          and exists (select m2.id from GroupChatMembers m2 where m2.room.id = r.id and m2.user.id = :b)
          and (select count(m3) from GroupChatMembers m3 where m3.room.id = r.id) = 2
    """)
    Optional<GroupChatRooms> findOneToOne(@Param("a") Long userA, @Param("b") Long userB);

    // ✅ 대기중 초대 방(정확히 inviter→invitee)
    @Query("""
        select r
        from GroupChatRooms r
        where r.roomName = concat('INVITE:', :inviterId, ':', :inviteeId)
          and r.createdBy.userType = boot.data.type.UserType.COMPANY
          and (select count(m) from GroupChatMembers m where m.room.id = r.id) = 1
    """)
    Optional<GroupChatRooms> findPendingInvite(@Param("inviterId") Long inviterId, @Param("inviteeId") Long inviteeId);

     // ✅ 내가 받은 대기중 초대 목록 (inviter는 아무 회사나 허용)
    @Query("""
        select r
        from GroupChatRooms r
        where r.roomName like concat('INVITE:%:', :inviteeId)
          and r.createdBy.userType = boot.data.type.UserType.COMPANY
          and (select count(m) from GroupChatMembers m where m.room.id = r.id) = 1
        order by r.id desc
    """)
    List<GroupChatRooms> findPendingInvitesForUser(@Param("inviteeId") Long inviteeId);
    
}
