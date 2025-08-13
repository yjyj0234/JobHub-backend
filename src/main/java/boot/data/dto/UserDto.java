package boot.data.dto;

import java.time.LocalDateTime;

import boot.data.entity.Users;
import boot.data.type.UserType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String email;
    private UserType userType;
    private boolean isActive;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static UserDto from(Users u) {
        return UserDto.builder()
            .id(u.getId())
            .email(u.getEmail())
            .userType(u.getUserType())
            .isActive(u.isActive())
            .emailVerifiedAt(toLdt(u.getEmailVerifiedAt()))
            .lastLoginAt(toLdt(u.getLastLoginAt()))
            .createdAt(toLdt(u.getCreatedAt()))
            .build();
    }

    private static LocalDateTime toLdt(Object t) {
        if (t == null) return null;
        if (t instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (t instanceof LocalDateTime ldt) return ldt;
        throw new IllegalArgumentException("Unsupported date type: " + t.getClass());
    }
}
