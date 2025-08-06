package boot.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "company_details")
public class CompanyDetails {
    
    @Id
    @Column(name = "company_id")
    private Long companyId;
    
    // Companies와의 OneToOne 관계 
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "company_id")
    private Companies company;
    
    @Column(name = "website_url", length = 255)
    private String websiteUrl;
    
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String mission;
    
    @Column(columnDefinition = "TEXT")
    private String culture;
    
    @Column(columnDefinition = "JSON")
    private String benefits;
}