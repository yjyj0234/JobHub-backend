package boot.data.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomResDto {
      private Long id;
    private String roomName;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Integer memberCount;
    private String lastMessage;
    private LocalDateTime lastSentAt;
}
