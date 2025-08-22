
package boot.data.dto.resume;

import java.time.LocalDate;

import boot.data.entity.ResumeExperiences.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExperienceRequest {
    @NotBlank
    private String companyName;

    private Long companyId;               
    @NotBlank
    private String position;

    private EmploymentType employmentType;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private boolean current;              // boolean 필드명 current (setCurrent 사용)

    private String description;
    private String achievements;
}
