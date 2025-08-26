package boot.data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FaqDto {
    private Long id;
    private String category;
    private String title;
    private String content;
}