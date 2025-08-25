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

     // ✅ 추가할 필드들 (JobPostingConditions에서 가져올 데이터)
     private String experienceLevel;    // 경력 레벨 (ENTRY, JUNIOR, MID 등)
     private String educationLevel;      // 학력 (ANY, HIGH_SCHOOL, COLLEGE 등)
     private String employmentType;      // 고용형태 (FULLTIME, CONTRACT 등)
     private String salaryType;          // 급여 타입 (FIXED, NEGOTIABLE 등)
     private Integer minSalary;          // 최소 급여
     private Integer maxSalary;          // 최대 급여
     private Short minExperienceYears;  // 최소 경력 년수
     private Short maxExperienceYears;  // 최대 경력 년수

}
