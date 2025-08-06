package boot.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "company_details")
public class CompanyDetails {
	
	@Id
	private Long company_id;
	
	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "company_id")
	private Companies company;
	
    @Column(name = "website_url", length = 255)
	private String website_url;
	
    @Lob
	private String logo_url;
	
    @Lob
	private String description;
	
    @Lob
	private String mission;
    @Lob
	private String culture;
	
    //JSON 필드는 String 으로 처리
    @Lob
	private String benefits;
	
	

}
