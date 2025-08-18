package boot.data.repository;

import boot.data.entity.GroupChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupChatMessagesRepository extends JpaRepository<GroupChatMessages, Long> {
    List<GroupChatMessages> findTop50ByRoom_IdOrderByIdDesc(Long roomId);
    List<GroupChatMessages> findByRoom_IdAndIdGreaterThanOrderByIdAsc(Long roomId, Long afterId);
    Optional<GroupChatMessages> findTop1ByRoom_IdOrderByIdDesc(Long roomId);
}
