package boot.data.dto.invitechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteActionDto {
    private Long roomId;   // 초대가 걸린 방 id (inviteId 대신 방 id 사용)
    private boolean accept;
}
