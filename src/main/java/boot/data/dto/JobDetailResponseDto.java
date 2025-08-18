package boot.data.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

import boot.data.entity.JobPostings;
import boot.data.entity.JobPostingCategories;
import boot.data.entity.JobPostingLocations;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDetailResponseDto {

    private Long id;
    private String title;

    // companies.name
    private String companyName;

    // 마감정보
    private String closeType;             // DEADLINE / UNTIL_FILLED / CONTINUOUS / PERIODIC
    private LocalDateTime closeDate;      // 엔티티가 LocalDateTime이므로 그대로 반환

    // 메타
    private Integer viewCount;
    private Integer applicationCount;

    // 본문
    private String description;

    // 프론트에서 표시용(없으면 렌더 안함)
    private List<LocationDto> locations;     // [{ regionId, name, isPrimary }]
    private List<CategoryDto> categories;    // [{ categoryId, name, isPrimary }]
    private List<String> skills;             // 엔티티에 없으면 빈 리스트
    private List<String> responsibilities;
    private List<String> qualifications;
    private List<String> preferences;
    private List<String> benefits;
    private String homepage;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LocationDto {
        private Integer regionId;    // regions.id (INT 스키마)
        private String name;         // regions.name
        private Boolean isPrimary;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryDto {
        private Integer categoryId;  // job_categories.id (INT 스키마)
        private String name;         // job_categories.name
        private Boolean isPrimary;
    }

    // ---- mapper ----
    public static JobDetailResponseDto from(
            JobPostings j,
            List<JobPostingLocations> locs,
            List<JobPostingCategories> cats
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

                // 아래 컬렉션/필드는 네 엔티티에 없으므로 기본값만
                .skills(Collections.emptyList())
                .responsibilities(Collections.emptyList())
                .qualifications(Collections.emptyList())
                .preferences(Collections.emptyList())
                .benefits(Collections.emptyList())
                .homepage(null)
                .build();
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? Collections.emptyList() : list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
