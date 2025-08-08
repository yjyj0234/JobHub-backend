package boot.data.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JobCategoryTreeDto {
    private Integer id;        // 100, 101 등
    private String name;       // 개발, 백엔드 등
    private Short level;       // 1: 대분류, 2: 소분류
    private Integer parentId;
    private List<JobCategoryTreeDto> children;  // 하위 직무들
}