package boot.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "companies")
@Entity //JPA Entity

public class Companies {

    @Id // 기본 키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private long id;
    
    @Column(nullable = false) // null 허용 안 함
    private String name; // 회사 이름

    @Column(name = "business_number", nullable = false, unique = true)
    private String businessNumber; // 사업자등록번호 (고유값)

    // industry_id (외래키)와 연결
    // 다대일 관계: 여러 회사가 하나의 업종(Industry)에 속할 수 있음
    // 자바에서는 industry.getName() 이런식으로 가져와서 자연스럽게 코드 작성을 하기위해 _를 생략하고 이름을 설정해줬습니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id") // 실제 FK 컬럼명
    private Industry industry;

    // company_size_id (외래키)와 연결
    // 다대일 관계: 여러 회사가 하나의 회사규모(CompanySize)에 속할 수 있음
    //마찬가지로 companySize도 자바에서 자연스럽게 불러올수있게 수정해줬음. 코드 작성할때 확인 필수
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_size_id")
    private CompanySize companySize;

    // 설립년도
    private Short foundedYear;

    // 인증 여부 (true / false)
    @Column(nullable = false)
    private boolean isVerified;
    
}
