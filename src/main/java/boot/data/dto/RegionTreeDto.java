package boot.data.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RegionTreeDto {
    private Integer id;        // 1000, 1010 등
    private String name;       // 서울특별시, 강남구 등
    private Short level;       // 1: 시/도, 2: 구/군
    private Integer parentId;  // 부모 ID
    private List<RegionTreeDto> children;  // 하위 지역들
}