package boot.data.dto.resume;

import java.time.LocalDate;

import boot.data.entity.ResumeExperiences.EmploymentType;
import lombok.Data;

@Data
public class ExperienceResponse {
    private Long id;
    private String companyName;
    private Long companyId;
    private String position;
    private EmploymentType employmentType;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
    private String description;
    private String achievements;
}