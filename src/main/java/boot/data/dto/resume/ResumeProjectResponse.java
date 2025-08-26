package boot.data.dto.resume;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeProjectResponse {
    Long id;
    Long resumeId;

    String projectName;
    String organization;
    String role;

    LocalDate startDate;
    LocalDate endDate;

    boolean ongoing;

    String projectUrl;
    String description;

    List<String> techStack;
}
