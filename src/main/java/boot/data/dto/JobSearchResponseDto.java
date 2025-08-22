package boot.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobSearchResponseDto {
    
    private Long id;
    private String title;
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private List<String> regions;       // 지역명 목록
    private List<String> categories;    // 직무명 목록
    private Integer viewCount;
    private Integer applicationCount;
    private LocalDateTime closeDate;
    private String closeType;
    private boolean isRemote;
    private LocalDateTime createdAt;
    private String status; 
}
