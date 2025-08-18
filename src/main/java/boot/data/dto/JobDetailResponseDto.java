// JobDetailResponseDto.java
package boot.data.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import boot.data.entity.JobPostings;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingLocations;
import boot.data.entity.JobPostingConditions;   // ✅ 추가
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDetailResponseDto {

    private Long id;
    private String title;
    private String companyName;

    private String closeType;        // DEADLINE / UNTIL_FILLED / CONTINUOUS / PERIODIC
    private LocalDateTime closeDate;

    private Integer viewCount;
    private Integer applicationCount;

    private String description;

    private List<LocationDto> locations;
    private List<CategoryDto> categories;

    // 기존 임시값(엔티티 없으면 빈 리스트 유지)
    private List<String> skills;
    private List<String> responsibilities;
    private List<String> qualifications;
    private List<String> preferences;
    private List<String> benefits;

    private String homepage;

    // ✅ 프론트에서 기대하는 중첩 객체
    private ConditionsDto conditions;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LocationDto {
        private Integer regionId;
        private String name;
        private Boolean isPrimary;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryDto {
        private Integer categoryId;
        private String name;
        private Boolean isPrimary;
    }

    // ✅ conditions 중첩 DTO (snake_case로 직렬화)
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ConditionsDto {
        @JsonProperty("employment_type")
        private String employmentType;

        @JsonProperty("education_level")
        private String educationLevel;

        @JsonProperty("experience_level")
        private String experienceLevel;

        @JsonProperty("min_experience_years")
        private Short minExperienceYears;

        @JsonProperty("max_experience_years")
        private Short maxExperienceYears;

        @JsonProperty("salary_type")
        private String salaryType;

        @JsonProperty("min_salary")
        private Integer minSalary;

        @JsonProperty("max_salary")
        private Integer maxSalary;

        @JsonProperty("work_schedule")
        private String workSchedule;

        private String etc;

        public static ConditionsDto from(JobPostingConditions c) {
            if (c == null) return null;
            return ConditionsDto.builder()
                    .employmentType(c.getEmploymentType() != null ? c.getEmploymentType().name() : null)
                    .educationLevel(c.getEducationLevel() != null ? c.getEducationLevel().name() : null)
                    .experienceLevel(c.getExperienceLevel() != null ? c.getExperienceLevel().name() : null)
                    .minExperienceYears(c.getMinExperienceYears())
                    .maxExperienceYears(c.getMaxExperienceYears())
                    .salaryType(c.getSalaryType() != null ? c.getSalaryType().name() : null)
                    .minSalary(c.getMinSalary())
                    .maxSalary(c.getMaxSalary())
                    .workSchedule(c.getWorkSchedule())
                    .etc(c.getEtc())
                    .build();
        }
    }

    // ✅ 새 오버로드: cond까지 받아서 세팅
    public static JobDetailResponseDto from(
            JobPostings j,
            List<JobPostingLocations> locs,
            List<JobPostingCategories> cats,
            JobPostingConditions cond
    ) {
        return JobDetailResponseDto.builder()
                .id(j.getId())
                .title(j.getTitle())
                .companyName(j.getCompany() != null ? j.getCompany().getName() : null)
                .closeType(j.getCloseType() != null ? j.getCloseType().name() : null)
                .closeDate(j.getCloseDate())
                .viewCount(j.getViewCount())
                .applicationCount(j.getApplicationCount())
                .description(j.getDescription())

                .locations(safe(locs).stream().map(l -> LocationDto.builder()
                        .regionId(l.getRegion() != null ? l.getRegion().getId() : null)
                        .name(l.getRegion() != null ? l.getRegion().getName() : null)
                        .isPrimary(l.isPrimary())
                        .build()
                ).collect(Collectors.toList()))

                .categories(safe(cats).stream().map(c -> CategoryDto.builder()
                        .categoryId(c.getJobCategory() != null ? c.getJobCategory().getId() : null)
                        .name(c.getJobCategory() != null ? c.getJobCategory().getName() : null)
                        .isPrimary(c.isPrimary())
                        .build()
                ).collect(Collectors.toList()))

                .skills(Collections.emptyList())
                .responsibilities(Collections.emptyList())
                .qualifications(Collections.emptyList())
                .preferences(Collections.emptyList())
                .benefits(Collections.emptyList())
                .homepage(null)

                .conditions(ConditionsDto.from(cond)) // ✅ 핵심
                .build();
    }

    // ✅ 기존 3-인자 버전은 호환용으로 유지(내부 위임)
    public static JobDetailResponseDto from(
            JobPostings j,
            List<JobPostingLocations> locs,
            List<JobPostingCategories> cats
    ) {
        return from(j, locs, cats, null);
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? Collections.emptyList()
                : list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ★ 새 오버로드: cond + 치환된 descriptionHtml 동시 주입
public static JobDetailResponseDto from(
        JobPostings j,
        List<JobPostingLocations> locs,
        List<JobPostingCategories> cats,
        JobPostingConditions cond,
        String descriptionHtml
) {
        String finalDesc = (descriptionHtml != null && !descriptionHtml.isBlank())
            ? descriptionHtml
            : j.getDescription();
    return JobDetailResponseDto.builder()
            .id(j.getId())
            .title(j.getTitle())
            .companyName(j.getCompany() != null ? j.getCompany().getName() : null)
            .closeType(j.getCloseType() != null ? j.getCloseType().name() : null)
            .closeDate(j.getCloseDate())
            .viewCount(j.getViewCount())
            .applicationCount(j.getApplicationCount())
            .description(finalDesc) // ← 치환된 HTML 반영

            .locations(safe(locs).stream().map(l -> LocationDto.builder()
                    .regionId(l.getRegion() != null ? l.getRegion().getId() : null)
                    .name(l.getRegion() != null ? l.getRegion().getName() : null)
                    .isPrimary(l.isPrimary())
                    .build()
            ).collect(Collectors.toList()))

            .categories(safe(cats).stream().map(c -> CategoryDto.builder()
                    .categoryId(c.getJobCategory() != null ? c.getJobCategory().getId() : null)
                    .name(c.getJobCategory() != null ? c.getJobCategory().getName() : null)
                    .isPrimary(c.isPrimary())
                    .build()
            ).collect(Collectors.toList()))

            .skills(Collections.emptyList())
            .responsibilities(Collections.emptyList())
            .qualifications(Collections.emptyList())
            .preferences(Collections.emptyList())
            .benefits(Collections.emptyList())
            .homepage(null)

            .conditions(ConditionsDto.from(cond))
            .build();
}
}
