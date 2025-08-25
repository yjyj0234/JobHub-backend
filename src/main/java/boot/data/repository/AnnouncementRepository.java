package boot.data.repository;

import boot.data.entity.Announcements;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcements, Long> {

    List<Announcements> findTop10ByOrderByCreatedAtDesc();

    List<Announcements> findAllByOrderByCreatedAtDesc();
}