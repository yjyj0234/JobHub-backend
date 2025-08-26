// src/main/java/boot/data/dto/resume/ActivityRequest.java
package boot.data.dto.resume;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ActivityRequest(
    @NotBlank String activityName,
    String organization,
    String role,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
    String description
) {}
