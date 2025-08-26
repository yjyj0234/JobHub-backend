package boot.data.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@AllArgsConstructor 
@NoArgsConstructor
public class CompanyProfileResponse {
    private CompanyProfileDto profile;

    // 부가정보
    private Long bookmarkCount;     // 전체 북마크 수
    private Boolean isBookmarked;   // 현재 로그인 유저가 북마크했는지
    private Integer openJobCount;   // 진행중 공고 수 (회사 칼럼이 있으면 그 값 혹은 재계산)
    private List<CompanyJobDto> recentOpenJobs; // 최근 OPEN 공고 6개
}

