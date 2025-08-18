package boot.data.repository;

import boot.data.entity.GroupChatMembers;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatMembersRepository extends JpaRepository<GroupChatMembers, Long> {
    boolean existsByRoom_IdAndUser_Id(Long roomId, Long userId);
    List<GroupChatMembers> findByRoom_Id(Long roomId);
    void deleteByRoom_IdAndUserId(Long roomId, Long userId);
    int countByRoom_Id(Long roomId);
}
