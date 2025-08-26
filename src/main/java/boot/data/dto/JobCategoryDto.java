package boot.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobCategoryDto {
    private Integer id;
    private String name;
    private Short level;
    private Integer parentId;
}