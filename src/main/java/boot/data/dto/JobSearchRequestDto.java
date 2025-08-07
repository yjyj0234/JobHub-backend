package boot.data.dto;

import java.util.List;

import lombok.Data;

@Data
public class JobSearchRequestDto {
    
    private String keyword;              // 키워드 검색
    private List<Integer> regionIds;    // 지역 ID 목록
    private List<Integer> categoryIds;  // 직무 카테고리 ID 목록
    private String employmentType;       // 고용 형태
    private String experienceLevel;      // 경력 수준
    private Integer minSalary;          // 최소 급여
    private Integer maxSalary;          // 최대 급여
    private Boolean isRemote;           // 재택근무 여부
    private String sortBy = "latest";   // 정렬 기준
    private Integer page = 0;           // 페이지 번호
    private Integer size = 20;          // 페이지 크기
}
