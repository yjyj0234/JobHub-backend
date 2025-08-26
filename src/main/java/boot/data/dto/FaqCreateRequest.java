package boot.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FaqCreateRequest {
    private String category;
    private String question;
    private String answer;
}