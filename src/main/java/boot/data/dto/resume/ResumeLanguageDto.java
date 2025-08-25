// src/main/java/boot/data/dto/ResumeLanguageDto.java
package boot.data.dto.resume;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ResumeLanguageDto {
    private Long id;

    @NotBlank(message = "언어명은 필수입니다.")
    private String language;

    private String proficiencyLevel; // 초급/중급/상급 등 자유 텍스트

    private String testName;
    private String testScore;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;
}
