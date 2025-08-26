// src/main/java/boot/data/dto/JobSearchRequestDto.java
package boot.data.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchRequestDto {
    
    private String keyword;
    
    @Builder.Default
    private List<Integer> regionIds = new ArrayList<>();
    
    @Builder.Default
    private List<Integer> categoryIds = new ArrayList<>();
    
    private String employmentType;
    private String experienceLevel;
    private Integer minSalary;
    private Integer maxSalary;
    private Boolean isRemote;
    
    @Builder.Default
    private String sortBy = "latest";
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
}