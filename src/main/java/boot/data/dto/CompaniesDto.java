package boot.data.dto;

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
@Table(name = "final")
@Entity

public class CompaniesDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(nullable = false)
    private String name;

    @Column(name = "business_number", nullable = false, unique = true)
    private String business_number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private IndustriesDto Industry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_size_id")
    private ComPany_SizesDto companySize;

    private Short founded_year;

    @Column(nullable = false)
    private boolean is_verified;
    
}
