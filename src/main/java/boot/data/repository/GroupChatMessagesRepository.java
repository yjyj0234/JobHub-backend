package boot.data.repository;

import boot.data.entity.GroupChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GroupChatMessagesRepository extends JpaRepository<GroupChatMessages, Long> {
    List<GroupChatMessages> findTop50ByRoom_IdOrderByIdDesc(Long roomId);
    List<GroupChatMessages> findByRoom_IdAndIdGreaterThanOrderByIdAsc(Long roomId, Long afterId);
    Optional<GroupChatMessages> findTop1ByRoom_IdOrderByIdDesc(Long roomId);

      @Modifying @Transactional
      void deleteByRoom_Id(Long roomId);
}
