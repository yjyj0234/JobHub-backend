// boot/data/dto/resume/ResumeCertificationRequest.java
package boot.data.dto.resume;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ResumeCertificationRequest {

    @Data
    public static class Create {
        @NotBlank
        private String certificationName;

        @NotBlank
        private String issuingOrganization;

        @NotNull
        private LocalDate issueDate;

        private LocalDate expiryDate;
        private String certificationNumber;
    }

    @Data
    public static class Update {
        @NotBlank
        private String certificationName;

        @NotBlank
        private String issuingOrganization;

        @NotNull
        private LocalDate issueDate;

        private LocalDate expiryDate;
        private String certificationNumber;
    }
}
