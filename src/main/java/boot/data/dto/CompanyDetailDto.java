// src/main/java/boot/data/dto/CompanyDetailDto.java
package boot.data.dto;

import lombok.Data;
import lombok.Builder;
import boot.data.entity.Companies;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDetailDto {
    // 기본 정보
    private Long id;
    private String name;
    private String industryName;
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
    
    // 통계 정보
    private Integer activeJobCount;
    private Double avgRating;
    
    public static CompanyDetailDto from(Companies company) {
        CompanyDetailDtoBuilder builder = CompanyDetailDto.builder()
            .id(company.getId())
            .name(company.getName())
            .foundedYear(company.getFoundedYear())
            .isVerified(company.isVerified())
            .activeJobCount(company.getActiveJobCount())
            .avgRating(company.getAvgRating() != null ? 
                company.getAvgRating().doubleValue() : null);
        
        // 업종 정보
        if (company.getIndustry() != null) {
            builder.industryName(company.getIndustry().getName());
        }
        
        // 규모 정보
        if (company.getCompanySize() != null) {
            builder.companySizeName(company.getCompanySize().getLabel());
        }
        
        // 상세 정보
        if (company.getCompanyDetails() != null) {
            var details = company.getCompanyDetails();
            builder.websiteUrl(details.getWebsiteUrl())
                .logoUrl(details.getLogoUrl())
                .description(details.getDescription())
                .mission(details.getMission())
                .culture(details.getCulture());
            
            // benefits JSON 파싱
            if (details.getBenefits() != null) {
                try {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<String> benefitsList = mapper.readValue(
                        details.getBenefits(), 
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                    builder.benefits(benefitsList);
                } catch (Exception e) {
                    builder.benefits(new ArrayList<>());
                }
            }
        }
        
        return builder.build();
    }
}