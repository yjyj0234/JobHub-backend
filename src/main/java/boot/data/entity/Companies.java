package boot.data.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Comment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "companies")
@Entity //JPA Entity

public class Companies {

    @Id // 기본 키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;
    
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

    /**
     * 회사 상세 정보 (1:1 관계)
     * LAZY로 설정하여 필요할 때만 조회
     */
    @OneToOne(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private CompanyDetails companyDetails;
     
    /**
     * 회사의 모든 채용공고 (1:N 관계)
     * 조회 시 사용 주의 (성능 이슈 가능)
     */
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @Comment("회사의 채용공고 목록")
    private List<JobPostings> jobPostings = new ArrayList<>();
    
    /**
     * 회사 평점 (리뷰 기반) - 계산된 필드
     */
    @Column(name = "avg_rating", precision = 2, scale = 1)
    @Comment("평균 평점 (1.0 ~ 5.0)")
    private BigDecimal avgRating;
    
    /**
     * 활성 채용공고 수 (캐시용)
     */
    @Column(name = "active_job_count")
    @Comment("현재 진행중인 채용공고 수")
    private Integer activeJobCount = 0;
    

    //owner_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", unique = true) // DB 스키마와 일치(UNIQUE)
    private Users owner;
    }
