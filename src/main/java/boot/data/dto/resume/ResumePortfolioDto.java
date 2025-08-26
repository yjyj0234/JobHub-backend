// src/main/java/boot/data/dto/ResumePortfolioDto.java
package boot.data.dto.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ResumePortfolioDto {
    private Long id;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다.")
    private String title;

    @NotBlank(message = "URL은 필수입니다.")
    @Size(max = 255, message = "URL은 255자를 넘을 수 없습니다.")
    private String url;

    private String description; // TEXT

    @Size(max = 50, message = "포트폴리오 유형은 50자를 넘을 수 없습니다.")
    private String portfolioType;
}
