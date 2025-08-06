package boot.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "company_sizes")
public class CompanySize {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //autoIncrement 인경우
    private Integer id;

    @Column(nullable = false,length = 100)
    private String label; //규모 이름 (스타트업, 중소기업, 대기업 등)

    @Column(name = "min_employees")
    private Integer minEmployees;  //최소 직원수

    @Column(name = "max_employees")
    private Integer maxEmployees; //최대 직원수

}
