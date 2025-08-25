// src/main/java/boot/data/dto/JobPostingSimpleDto.java
package boot.data.dto;

import lombok.Data;
import lombok.Builder;
import boot.data.entity.JobPostings;
import boot.data.entity.JobPostingConditions;
import boot.data.type.PostingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobPostingSimpleDto {
    private Long id;
    private String title;
    private String employmentType;  // String으로 변경
    private String experienceLevel;  // String으로 변경
    private String salary;  // 계산된 급여 문자열
    private PostingStatus status;
    private LocalDateTime closeDate;  // deadlineDate -> closeDate
    private LocalDateTime createdAt;
    
    // 주요 지역 (primary)
    private String primaryLocation;
    
    // 주요 직무 (primary)
    private String primaryJobCategory;
    
    // 지원자 수
    private Integer applicationCount;
    
    public static JobPostingSimpleDto from(JobPostings posting) {
        JobPostingSimpleDtoBuilder builder = JobPostingSimpleDto.builder()
            .id(posting.getId())
            .title(posting.getTitle())
            .status(posting.getStatus())
            .closeDate(posting.getCloseDate())
            .createdAt(posting.getCreatedAt())
            .applicationCount(posting.getApplicationCount());
        
        // JobPostingConditions에서 정보 가져오기
        JobPostingConditions conditions = posting.getJobPostingConditions();
        if (conditions != null) {
            // employmentType
            if (conditions.getEmploymentType() != null) {
                builder.employmentType(conditions.getEmploymentType().name());
            }
            
            // experienceLevel
            if (conditions.getExperienceLevel() != null) {
                builder.experienceLevel(conditions.getExperienceLevel().name());
            }
            
            // salary 계산
            String salaryStr = calculateSalary(
                conditions.getMinSalary(), 
                conditions.getMaxSalary(),
                conditions.getSalaryType() != null ? conditions.getSalaryType().name() : null
            );
            builder.salary(salaryStr);
        }
        
        // 주요 지역 (isPrimary = true인 것 찾기)
        posting.getJobPostingLocations().stream()
            .filter(loc -> loc.isPrimary())
            .findFirst()
            .ifPresent(loc -> {
                if (loc.getRegion() != null) {
                    builder.primaryLocation(loc.getRegion().getName());
                }
            });
        
        // 주요 직무 (isPrimary = true인 것 찾기)
        posting.getJobPostingCategories().stream()
            .filter(cat -> cat.isPrimary())
            .findFirst()
            .ifPresent(cat -> {
                if (cat.getJobCategory() != null) {
                    builder.primaryJobCategory(cat.getJobCategory().getName());
                }
            });
        
        return builder.build();
    }
    
    private static String calculateSalary(Integer minSalary, Integer maxSalary, String salaryType) {
        if ("NEGOTIABLE".equals(salaryType)) {
            return "면접 후 협의";
        }
        
        if (minSalary == null && maxSalary == null) {
            return "회사내규";
        }
        
        // 만원 단위로 변환
        if (minSalary != null && maxSalary != null && minSalary.equals(maxSalary)) {
            return (minSalary / 10000) + "만원";
        } else if (minSalary != null && maxSalary != null) {
            return (minSalary / 10000) + "~" + (maxSalary / 10000) + "만원";
        } else if (minSalary != null) {
            return (minSalary / 10000) + "만원 이상";
        } else if (maxSalary != null) {
            return "~" + (maxSalary / 10000) + "만원";
        }
        
        return "회사내규";
    }
}