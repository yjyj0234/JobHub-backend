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
}
