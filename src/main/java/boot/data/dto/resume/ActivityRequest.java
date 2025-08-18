package boot.data.dto.resume;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;

/**
 * resume_activities 생성/수정 요청 DTO
 */
public record ActivityRequest(
        @NotBlank String activityName,
        String organization,
        String role,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,
        String description
) {}
