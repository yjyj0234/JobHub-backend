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
	@Column(name = "company_id")
	private Long companyId; // camelCase로 변경
	
	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "company_id")
	private Companies company;
	
	@Column(name = "website_url", length = 255)
	private String websiteUrl; // camelCase로 변경
	
	@Lob
	@Column(name = "logo_url", columnDefinition = "TEXT")
	private String logoUrl; // camelCase로 변경
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String mission;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String culture;
	
	// JSON 필드를 String으로 처리 (MySQL JSON 컬럼)
	// 복리후생 데이터 예시:
	@Lob
	@Column(columnDefinition = "JSON")
	private String benefits;
}
