package boot.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqCreateRequest {
    private String category;
    private String title;
    private String content;
}