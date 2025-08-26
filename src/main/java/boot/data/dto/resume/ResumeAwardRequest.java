// package boot.data.dto.request;
package boot.data.dto.resume;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ResumeAwardRequest {

    @Data
    public static class Create {
        @NotBlank(message = "수상명은 필수입니다.")
        private String awardName;

        @NotBlank(message = "주최/기관은 필수입니다.")
        private String organization;

        @NotNull(message = "수상일은 필수입니다.")
        private LocalDate awardDate;

        private String description;
    }

    @Data
    public static class Update {
        @NotBlank(message = "수상명은 필수입니다.")
        private String awardName;

        @NotBlank(message = "주최/기관은 필수입니다.")
        private String organization;

        @NotNull(message = "수상일은 필수입니다.")
        private LocalDate awardDate;

        private String description;
    }
}
