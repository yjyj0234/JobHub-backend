package boot.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRankingDto {
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private String industry;
    private Long bookmarkCount;
    private Integer ranking;
}