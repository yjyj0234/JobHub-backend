
package boot.data.dto.resume;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillResponse {
    private Integer id;
    private String name;
    private Integer categoryId;
    private boolean isVerified;
}
