package boot.data.dto;

import java.util.List;

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
    private boolean isPrimary = false;

    //공개여부 , true: 다른사용자/기업이 볼 수 있음.  false: 본인만 볼 수 있음.
    private boolean isPublic =false;

    //이력서 현재 작성중인지 여부 "작성중" 또는 "작성완료"
    private String status = "작성중";

    

    // userId는 JWT 쿠키에서 추출하므로 DTO에 포함하지 않는다
}
