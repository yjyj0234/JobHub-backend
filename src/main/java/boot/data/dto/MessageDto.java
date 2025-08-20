package boot.data.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
     private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private LocalDateTime sentAt;
    private String message;
    private boolean mine;

    //채팅방 나가면 알림
    private boolean system;     // ✅ 시스템 메시지 여부
    private String type;        // "SYSTEM" | "CHAT" 등 (선택)
}
