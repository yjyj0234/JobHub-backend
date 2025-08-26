package boot.data.dto.resume;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResumeProjectRequest {

    @NotBlank(message = "프로젝트명은 필수입니다.")
    private String projectName;

    private String organization;
    private String role;

    private LocalDate startDate;
    private LocalDate endDate;

    /** 진행중 여부 */
    private Boolean ongoing;

    private String projectUrl;
    private String description;

    /** 사용 기술 스택 (문자열 리스트) */
    private List<String> techStack;
}
