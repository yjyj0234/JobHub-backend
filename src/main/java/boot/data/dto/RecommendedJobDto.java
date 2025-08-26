package boot.data.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedJobDto {
    private Long jobPostingId;
    private String title;
    private String companyName;
    private String companyLogo;
    private List<String> categories;  // 직무 카테고리들
    private List<String> regions;      // 지역들
    private LocalDateTime closeDate;
    private Integer viewCount;
    private boolean isLiked;          // 현재 유저가 좋아요했는지
}