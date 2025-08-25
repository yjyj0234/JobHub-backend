package boot.data.dto.invitechat;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteResDto {
     private Long roomId;
    private Long inviterId;
    private Long inviteeId;
    private String status;            // PENDING / ACCEPTED / DECLINED (DECLINED은 즉시 삭제되므로 응답 시점에만)
    private LocalDateTime createdAt;  // 방 생성 시각
    private LocalDateTime respondedAt; // 수락/거절 시각 (수락 시 now, 거절은 null로 둬도 OK)
}
