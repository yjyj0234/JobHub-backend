package boot.data.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyPostingListDto {
    private Long id;
    private String title;
    private String status;
    private Integer viewCount;
    private Integer applicationCount;
    private String createdAt;
    private String closeDate;
    private List<String> locations;
    private String employmentType;
}
