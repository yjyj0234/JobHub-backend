package boot.data.dto.resume;

import java.time.LocalDateTime;

import boot.data.entity.Resumes;

public record ResumeResponse(
        Long id,
        Long userId,
        String title,
        boolean isPrimary,
        boolean isPublic,
        short completionRate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResumeResponse from(Resumes r) {
        return new ResumeResponse(
                r.getId(),
                r.getUser().getId(),
                r.getTitle(),
                r.isPrimary(),
                r.isPublic(),
                r.getCompletionRate(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}