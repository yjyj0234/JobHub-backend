package boot.data.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class ChatMessageDto {
    private Long id;             // 메시지 PK
    private Long userId;         // 보낸 유저 ID
    private String roomKey;      // 방 식별자
    private String message;      // 본문
    private LocalDateTime sentAt; // 전송 시각
}
