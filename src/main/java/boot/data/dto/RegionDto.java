package boot.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegionDto {
    private Integer id;
    private String name;
    private Short level;
    private Integer parentId;
}