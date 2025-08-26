package boot.data.controller;

import boot.data.dto.AnnouncementMessage;
import boot.data.entity.Announcements;
import boot.data.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Announcements> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/latest")
    @Transactional(readOnly = true)
    public List<AnnouncementMessage> getLatestAnnouncements() {
        return announcementRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(entity -> new AnnouncementMessage(entity.getTitle(), entity.getContent(), entity.getUrl()))
                .collect(Collectors.toList());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Announcements> createAndSendAnnouncement(@RequestBody AnnouncementMessage announcementDto) {
        Announcements newAnnouncement = new Announcements();
        newAnnouncement.setTitle(announcementDto.getTitle());
        newAnnouncement.setContent(announcementDto.getContent());
        newAnnouncement.setUrl(announcementDto.getUrl());
        Announcements savedAnnouncement = announcementRepository.save(newAnnouncement);

        messagingTemplate.convertAndSend("/topic/announcements", announcementDto);

        return ResponseEntity.ok(savedAnnouncement);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Announcements> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementMessage announcementDto) {
        return announcementRepository.findById(id)
                .map(existingAnnouncement -> {
                    existingAnnouncement.setTitle(announcementDto.getTitle());
                    existingAnnouncement.setContent(announcementDto.getContent());
                    existingAnnouncement.setUrl(announcementDto.getUrl());
                    Announcements updated = announcementRepository.save(existingAnnouncement);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        if (!announcementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        announcementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}