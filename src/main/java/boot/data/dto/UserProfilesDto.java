package boot.data.dto;

import java.time.LocalDate;

import boot.data.entity.UserProfiles;

public record UserProfilesDto(
    Long userId,
    String name,
    String phone,
    Short birthYear,         // (호환) 추후 제거 가능
    String profileImageUrl,
    String headline,
    String summary,
    Integer regionId,        // Integer로 통일
    String regionName,
    LocalDate birthDate      // ✅ 신규
) {
    public static UserProfilesDto from(UserProfiles p) {
        return new UserProfilesDto(
            p.getUserId(),
            p.getName(),
            p.getPhone(),
            p.getBirthYear(),
            p.getProfileImageUrl(),
            p.getHeadline(),
            p.getSummary(),
            p.getRegion() != null ? p.getRegion().getId() : null,
            p.getRegion() != null ? p.getRegion().getName() : null,
            p.getBirthDate()
        );
    }
}
