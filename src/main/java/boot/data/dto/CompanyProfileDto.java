// src/main/java/boot/data/dto/CompanyProfileDto.java
package boot.data.dto;

import lombok.Data;
import lombok.Builder;
import boot.data.entity.Companies;
import boot.data.entity.CompanyDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyProfileDto {
    // 기본 정보
    private Long id;
    private String name;
    private String businessNumber;
    private Long industryId;
    private String industryName;
    private Integer companySizeId;
    private String companySizeName;
    private Short foundedYear;
    private boolean isVerified;
    
    // 상세 정보
    private String websiteUrl;
    private String logoUrl;
    private String description;
    private String mission;
    private String culture;
    private List<String> benefits;
    
    // 통계 정보 (조회시만)
    private Integer activeJobCount;
    private Double avgRating;
    
    public static CompanyProfileDto from(Companies company) {
        CompanyProfileDtoBuilder builder = CompanyProfileDto.builder()
            .id(company.getId())
            .name(company.getName())
            .businessNumber(company.getBusinessNumber())
            .foundedYear(company.getFoundedYear())
            .isVerified(company.isVerified())
            .activeJobCount(company.getActiveJobCount())
            .avgRating(company.getAvgRating() != null ? company.getAvgRating().doubleValue() : null);
        
        // 업종 정보
        if (company.getIndustry() != null) {
            builder.industryId(company.getIndustry().getId())
                .industryName(company.getIndustry().getName());
        }
        
        // 규모 정보
        if (company.getCompanySize() != null) {
            builder.companySizeId(company.getCompanySize().getId())
                   .companySizeName(company.getCompanySize().getLabel());  // 🔥 label로 수정
        }
        
        // 상세 정보
        CompanyDetails details = company.getCompanyDetails();
        if (details != null) {
            builder.websiteUrl(details.getWebsiteUrl())
                .logoUrl(details.getLogoUrl())
                .description(details.getDescription())
                .mission(details.getMission())
                .culture(details.getCulture());
            
            // benefits JSON 파싱
            if (details.getBenefits() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<String> benefitsList = mapper.readValue(details.getBenefits(), 
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    builder.benefits(benefitsList);
                } catch (Exception e) {
                    builder.benefits(new ArrayList<>());
                }
            }
        }
        
        return builder.build();
    }
}