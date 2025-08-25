// package boot.data.dto.response;
package boot.data.dto.resume;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumeAwardResponse {
    private Long id;
    private String awardName;
    private String organization;
    private LocalDate awardDate;
    private String description;
}
