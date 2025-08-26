// boot/data/dto/resume/ResumeCertificationResponse.java
package boot.data.dto.resume;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeCertificationResponse {
    private Long id;
    private String certificationName;
    private String issuingOrganization;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String certificationNumber;
}
