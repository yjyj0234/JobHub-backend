// src/main/java/boot/data/dto/resume/EducationResponse.java
package boot.data.dto.resume;

import java.math.BigDecimal;
import java.time.LocalDate;

import boot.data.entity.ResumeEducations;

public record EducationResponse(
        Long id,
        Long resumeId,
        String schoolName,
        String schoolType,
        String major,
        String minor,
        String degree,
        LocalDate admissionDate,
        LocalDate graduationDate,
        String graduationStatus,
        BigDecimal gpa,
        BigDecimal maxGpa
) {
    public static EducationResponse from(ResumeEducations e) {
        return new EducationResponse(
                e.getId(),
                e.getResume() != null ? e.getResume().getId() : null,
                e.getSchoolName(),
                e.getSchoolType(),
                e.getMajor(),
                e.getMinor(),
                e.getDegree(),
                e.getAdmissionDate(),
                e.getGraduationDate(),
                e.getGraduationStatus(),
                e.getGpa(),
                e.getMaxGpa()
        );
    }
}
