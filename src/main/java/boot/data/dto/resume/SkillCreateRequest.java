// src/main/java/boot/data/dto/SkillCreateRequest.java
package boot.data.dto.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    private Integer categoryId; // nullable
    private Boolean isVerified; // 선택값(없으면 false로 처리)
}
