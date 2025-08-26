package boot.data.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
public class CompanyProfileRequest {
    // 기본 정보
    @NotBlank(message = "기업명은 필수입니다")
    @Size(min = 2, max = 100, message = "기업명은 2-100자 사이여야 합니다")
    private String name;
    
    @NotBlank(message = "사업자등록번호는 필수입니다")
    @Pattern(regexp = "\\d{3}-\\d{2}-\\d{5}", message = "사업자등록번호 형식이 올바르지 않습니다 (예: 123-45-67890)")
    private String businessNumber;
    
    @NotNull(message = "업종 선택은 필수입니다")
    private Long industryId;
    
    @NotNull(message = "기업 규모 선택은 필수입니다")
    private Integer companySizeId;  // 🔥 Integer로 수정 (CompanySize의 id가 Integer)
    
    @Min(value = 1900, message = "설립년도는 1900년 이상이어야 합니다")
    @Max(value = 2100, message = "설립년도가 올바르지 않습니다")
    private Short foundedYear;
    
    // 상세 정보 (선택사항)
    @Pattern(regexp = "^$|^(https?://).*", message = "웹사이트 URL 형식이 올바르지 않습니다")
    private String websiteUrl;
    
    private String logoUrl;
    
    @Size(max = 1000, message = "기업 소개는 1000자 이내여야 합니다")
    private String description;
    
    @Size(max = 500, message = "미션은 500자 이내여야 합니다")
    private String mission;
    
    @Size(max = 500, message = "문화 소개는 500자 이내여야 합니다")
    private String culture;
    
    @Size(max = 20, message = "복지 항목은 최대 20개까지 입력 가능합니다")
    private List<String> benefits;
}