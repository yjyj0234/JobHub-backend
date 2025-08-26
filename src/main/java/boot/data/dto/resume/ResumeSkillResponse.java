
package boot.data.dto.resume;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeSkillResponse {
    private Long id;          // resume-skill 링크 id (FE에서 delete용)
    private Integer skillId;  // skills.id
    private String name;
    private Integer categoryId;
    private boolean isVerified;
}
