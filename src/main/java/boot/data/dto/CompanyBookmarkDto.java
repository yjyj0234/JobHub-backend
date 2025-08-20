package boot.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBookmarkDto {
    private Long companyId;
    private String companyName;
    private boolean isBookmarked;
    private Long bookmarkCount;  // 해당 기업의 총 북마크 수
}