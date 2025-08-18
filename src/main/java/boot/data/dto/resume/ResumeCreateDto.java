package boot.data.dto.resume;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResumeCreateDto {
    
    //이력서 제목은 필수로 입력하게 만들기
    @NotBlank(message = "이력서 제목은 필수입니다")
    @Size(max = 255,message = "이력서 제목은 255자 이하여야 합니다")
    private String title;

    //대표 이력서 여부, true로 설정시 기존 대표 이력서는 자동으로 해체됨
     @JsonProperty("isPrimary")
    private boolean isPrimary = false;

    //공개여부 , true: 다른사용자/기업이 볼 수 있음.  false: 본인만 볼 수 있음.
    @JsonProperty("isPublic")
    private boolean isPublic =false;

    //이력서 현재 작성중인지 여부 "작성중" 또는 "작성완료"
  // ✅ 프론트에서 계산한 완성도 값을 받음(0~100)
    @Min(0) @Max(100)
    private Short completionRate = 0;

    

    // userId는 JWT 쿠키에서 추출하므로 DTO에 포함하지 않는다
}
