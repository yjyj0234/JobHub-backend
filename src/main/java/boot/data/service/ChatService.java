package boot.data.service;

import java.time.LocalDateTime;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.ChatMessageDto;
import boot.data.dto.ChatSendRequest;
import boot.data.entity.OneToOneChatRooms;
import boot.data.entity.Users;
import boot.data.repository.OneToOneChatRoomsRepository;
import boot.data.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final OneToOneChatRoomsRepository chatRepo;
    private final UsersRepository usersRepo;

    @Transactional
    public ChatMessageDto saveMessage(ChatSendRequest req) {
        // 유저 확인
        Users user = usersRepo.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        OneToOneChatRooms entity = new OneToOneChatRooms();
        entity.setUser(user);
        entity.setRoomKey(req.getRoomKey());
        entity.setMessage(req.getMessage());
        entity.setSentAt(LocalDateTime.now());

        OneToOneChatRooms saved = chatRepo.save(entity);

        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(saved.getId());
        dto.setUserId(user.getId());
        dto.setRoomKey(saved.getRoomKey());
        dto.setMessage(saved.getMessage());
        dto.setSentAt(saved.getSentAt());
        return dto;
    }
    
}
