package boot.data.dto;


import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import boot.data.entity.Applications;
import boot.data.type.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationResponse {
    private Long id;
    private Long postingId;
    private Long resumeId;
    private Long userId;
    private ApplicationStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp appliedAt;
    private Timestamp viewdAt;

    public static ApplicationResponse from(Applications a){
        return ApplicationResponse.builder()
               .id(a.getId())
               .postingId(a.getJobPosting() != null ? a.getJobPosting().getId() : null)
                .resumeId(a.getResume() != null ? a.getResume().getId() : null)
                .userId(a.getUser() != null ? a.getUser().getId() : null)
                .status(a.getStatus())
                .appliedAt(a.getAppliedAt())
                .viewdAt(a.getViewedAt())
                .build();
    }
}
