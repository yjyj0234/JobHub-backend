package boot.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import boot.data.entity.OneToOneChatRooms;

@Repository
public interface OneToOneChatRoomsRepository extends JpaRepository<OneToOneChatRooms, Long> {

    // 방 기준 시간순 조회
    List<OneToOneChatRooms> findByRoomKeyOrderBySentAtAsc(String roomKey);


    // 방 기준 시간 역순 조회
    List<OneToOneChatRooms> findTop200ByRoomKeyOrderBySentAtDesc(String roomKey);
}