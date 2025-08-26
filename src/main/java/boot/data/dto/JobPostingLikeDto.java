package boot.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingLikeDto {
    private Long jobPostingId;
    private boolean isLiked;
    private Long likeCount;  // 해당 공고의 총 좋아요 수
}