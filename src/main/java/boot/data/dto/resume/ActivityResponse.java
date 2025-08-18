package boot.data.dto.resume;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import boot.data.entity.ResumeActivity;

/**
 * resume_activities 응답 DTO
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActivityResponse(
        Long id,
        Long resumeId,
        String activityName,
        String organization,
        String role,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,
        String description
) {
    public static ActivityResponse from(ResumeActivity a) {
        return new ActivityResponse(
                a.getId(),
                a.getResume().getId(),
                a.getActivityName(),
                a.getOrganization(),
                a.getRole(),
                a.getStartDate(),
                a.getEndDate(),
                a.getDescription()
        );
    }
    
}
