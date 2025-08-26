package boot.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import boot.data.type.CloseType;
import boot.data.type.EducationLevel;
import boot.data.type.EmploymentType;
import boot.data.type.ExperienceLevel;
import boot.data.type.PostingStatus;
import boot.data.type.SalaryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPostingCreateDto {
    
   
    @Null(message = "서버에서 설정합니다")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long companyId;

    @Null(message = "서버에서 설정합니다")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long createdBy; // Users.id

    @NotBlank
    private String title;

    @NotNull
    private PostingStatus status;

    @NotNull
    private CloseType closeType;

    @NotNull
    private Boolean isRemote;

    private LocalDateTime openDate;   // "YYYY-MM-DDTHH:mm" 자동 바인딩
    private LocalDateTime closeDate;

    @NotNull
    private RegionSelection regions;

    @NotNull
    @Size(min = 1)
    private List<CategoryItem> categories;

    @NotNull
    private Conditions conditions;

    private String description;   // 스키마에 컬럼 없으면 서비스에서 무시
    private String searchText;    // 없으면 title로 대체

    // === nested DTOs ===
    @Data
    public static class RegionSelection {
        private Integer sidoId;
        private Integer sigunguId;
    }

    @Data
    public static class CategoryItem {
        @NotNull
        private Integer categoryId;
        @NotNull
        private Boolean isPrimary;
    }

    @Data
    public static class Conditions {
        @NotNull
        private Short minExperienceYears;
        private Short maxExperienceYears;
        private Integer minSalary;
        private Integer maxSalary;

        @NotNull private SalaryType salaryType;
        @NotNull private EmploymentType employmentType;
        @NotNull private ExperienceLevel experienceLevel;
        @NotNull private EducationLevel educationLevel;

        @NotBlank private String workSchedule;
        @NotBlank private String etc;
    }

}
