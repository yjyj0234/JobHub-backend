// src/main/java/boot/data/dto/resume/EducationRequest.java
package boot.data.dto.resume;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record EducationRequest(
        @NotBlank String schoolName,
        String schoolType,
        String major,
        String minor,
        String degree,
        LocalDate admissionDate,
        LocalDate graduationDate,
        String graduationStatus,   // 예: ENROLLED/GRADUATED... 또는 "재학" 등
        BigDecimal gpa,
        BigDecimal maxGpa
) {}
