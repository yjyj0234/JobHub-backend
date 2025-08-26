package boot.data.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqDto {
    private Long id;
    private String category;
    private String question;
    private String answer;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}