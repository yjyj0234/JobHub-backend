// src/main/java/boot/data/dto/JobDetailResponseDto.java
package boot.data.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import boot.data.entity.JobPostings;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingLocations;
import boot.data.entity.JobPostingConditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDetailResponseDto {

    private Long id;
    private String title;
    private String companyName;

    private Long companyId;
    // 🔥 회사 소유자 ID 추가
    private Long companyOwnerId;

    /** DEADLINE / UNTIL_FILLED / CONTINUOUS / PERIODIC */
    private String closeType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeDate;

    private Integer viewCount;
    private Integer applicationCount;

    /** presigned URL 등이 반영된 최종 HTML */
    private String description;

    /** 대표 1개만 내려도 되지만, 호환 위해 List 유지 */
    private List<LocationDto> locations;
    private List<CategoryDto> categories;

    // 임시/옵션 필드들(엔티티 없으면 빈 리스트)
    private List<String> skills;
    private List<String> responsibilities;
    private List<String> qualifications;
    private List<String> preferences;
    private List<String> benefits;

    private String homepage;

    /** 프론트에서 기대하는 중첩 객체(스네이크 케이스 직렬화) */
    private ConditionsDto conditions;

    /** 재택근무 가능 여부 (DB tinyint(1): 1=가능, 0=불가) */
    private Boolean isRemote;

    // ------------------------
    // Nested DTOs
    // ------------------------

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private Integer regionId;
        private String name;       // 예: "서울특별시 강남구" (백엔드에서 합성)
        private Boolean isPrimary; // 대표 지역 여부
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Integer categoryId;
        private String name;
        private Boolean isPrimary;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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

    // ------------------------
    // Factory methods
    // ------------------------

    /**
     * 이미 합성된 지역명(LocationDto)을 전달받는 버전.
     * 제네릭 타입 소거 충돌을 피하기 위해 이름을 fromResolvedLocations 로 분리.
     */
    public static JobDetailResponseDto fromResolvedLocations(
            JobPostings j,
            List<LocationDto> locs,
            List<JobPostingCategories> cats,
            JobPostingConditions cond,
            String descriptionHtml
    ) {
        String finalDesc = (descriptionHtml != null && !descriptionHtml.isBlank())
                ? descriptionHtml
                : (j.getDescription() != null ? j.getDescription() : null);

        return JobDetailResponseDto.builder()
                .id(j.getId())
                .title(j.getTitle())
                .companyName(j.getCompany() != null ? j.getCompany().getName() : null)
                .companyId(j.getCompany() != null ? j.getCompany().getId() : null)
                // 🔥 회사 소유자 ID 추가 - Companies 엔티티의 owner.id 사용
                .companyOwnerId(j.getCompany() != null && j.getCompany().getOwner() != null ? 
                    j.getCompany().getOwner().getId() : null)
                .closeType(j.getCloseType() != null ? j.getCloseType().name() : null)
                .openDate(j.getOpenDate())
                .closeDate(j.getCloseDate())
                .viewCount(j.getViewCount())
                .applicationCount(j.getApplicationCount())
                .description(finalDesc)

                // 이미 완성된 LocationDto 리스트 그대로 사용
                .locations(safe(locs))

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
                .isRemote(j.isRemote()) // or (j.getIsRemote() == 1)
                .build();
    }

    /**
     * 엔티티(JobPostingLocations)를 그대로 받아서 DTO로 변환하는 일반 버전.
     */
    public static JobDetailResponseDto from(
            JobPostings j,
            List<JobPostingLocations> locs,
            List<JobPostingCategories> cats,
            JobPostingConditions cond,
            String descriptionHtml
    ) {
        String finalDesc = (descriptionHtml != null && !descriptionHtml.isBlank())
                ? descriptionHtml
                : (j.getDescription() != null ? j.getDescription() : null);

        return JobDetailResponseDto.builder()
                .id(j.getId())
                .title(j.getTitle())
                .companyName(j.getCompany() != null ? j.getCompany().getName() : null)
                // 🔥 회사 소유자 ID 추가 - Companies 엔티티의 owner.id 사용
                .companyOwnerId(j.getCompany() != null && j.getCompany().getOwner() != null ? 
                    j.getCompany().getOwner().getId() : null)
                .closeType(j.getCloseType() != null ? j.getCloseType().name() : null)
                .openDate(j.getOpenDate())
                .closeDate(j.getCloseDate())
                .viewCount(j.getViewCount())
                .applicationCount(j.getApplicationCount())
                .description(finalDesc)

                .locations(safe(locs).stream().map(l -> LocationDto.builder()
                        .regionId(l.getRegion() != null ? l.getRegion().getId() : null)
                        // 여기서는 "부모 + 자식" 합성을 서비스에서 해오지 않는다면 child 이름만 내려감.
                        // 합성본을 쓰고 싶다면 JobDetailService에서 만든 fullName을 LocationDto.name 으로 넘기고,
                        // 이 메서드 대신 fromResolvedLocations(...)을 사용하세요.
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
                .isRemote(j.isRemote())
                .build();
    }

    /**
     * 호환용(예전 호출부): cond/description 없이 호출 → 내부 위임
     */
    public static JobDetailResponseDto from(
            JobPostings j,
            List<JobPostingLocations> locs,
            List<JobPostingCategories> cats
    ) {
        return from(j, locs, cats, null, null);
    }

    // ------------------------
    // Utils
    // ------------------------

    private static <T> List<T> safe(List<T> list) {
        return list == null
                ? Collections.emptyList()
                : list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}