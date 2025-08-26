package boot.data.dto;

import boot.data.entity.JobPostings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class CompanyJobDto {
    private Long id;
    private String title;
    private String status;     // OPEN/CLOSED/EXPIRED...
    private String closeType;  // DEADLINE/CONTINUOUS/UNTIL_FILLED/PERIODIC...
    private String closeDate;  // 문자열(ISO)로 내려서 프론트에서 바로 사용
    private Integer viewCount;

    public static CompanyJobDto from(JobPostings jp) {
        return CompanyJobDto.builder()
                .id(jp.getId())
                .title(jp.getTitle())
                .status(jp.getStatus() != null ? jp.getStatus().name() : null)
                .closeType(jp.getCloseType() != null ? jp.getCloseType().name() : null)
                .closeDate(jp.getCloseDate() != null ? jp.getCloseDate().toString() : null)
                .viewCount(jp.getViewCount())
                .build();
    }
}
