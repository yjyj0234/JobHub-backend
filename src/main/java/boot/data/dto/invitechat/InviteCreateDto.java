package boot.data.dto.invitechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteCreateDto {
    private Long targetUserId;   // 초대할 USER id
    private String message;      // 선택: 초대 메시지
}
