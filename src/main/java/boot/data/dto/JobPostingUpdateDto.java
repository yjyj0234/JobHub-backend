package boot.data.dto;

import boot.data.type.CloseType;
import lombok.Data;

@Data
public class JobPostingUpdateDto {
    private String title;
    private String description;
    private boolean isRemote;
    private CloseType closeType;
    // 필요한 필드 추가
}