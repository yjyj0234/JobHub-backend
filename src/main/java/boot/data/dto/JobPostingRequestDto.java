package boot.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import boot.data.entity.JobPostingConditions.EducationLevel;
import boot.data.entity.JobPostingConditions.EmploymentType;
import boot.data.entity.JobPostingConditions.ExperienceLevel;
import boot.data.entity.JobPostingConditions.SalaryType;
import boot.data.type.CloseType;
import boot.data.type.PostingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Enum 타입들은 여기에 직접 정의하거나, 별도의 파일로 관리할 수 있습니다.
// 간결성을 위해 여기에 정의합니다.

/**
 * 채용 공고 생성을 위한 단일 요청 DTO
 * JobPostings와 JobPostingConditions의 모든 필드를 포함합니다.
 */
@Getter
@Setter
public class JobPostingRequestDto {
	
    // --- JobPostings 관련 필드 ---
    private String title;
    private PostingStatus status;
    private CloseType closeType;
    
    @JsonProperty("is_remote")
    private boolean isRemote;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;

    // --- JobPostingConditions 관련 필드 ---
    private String workSchedule;
    private EmploymentType employmentType;
    private ExperienceLevel experienceLevel;
    private Short minExperienceYears;
    private Short maxExperienceYears;
    private EducationLevel educationLevel;
    private SalaryType salaryType;
    private Integer minSalary;
    private Integer maxSalary;
    private String etc;
    
    // 이 외에 React 폼에서 넘어오지는 않지만, 서버에서 설정해야 할 값들
    // 예: private Long companyId;
    // 예: private Long createdById;
}