package boot.data.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "user_profiles")
public class UserProfiles {
	
	@Id	
	private Long user_id;
	
    // Users 테이블의 기본 키를 외래 키로 사용하고, 엔티티 간 1:1 관계 설정
	@OneToOne(fetch = FetchType.LAZY)
	@MapsId		//User 엔티티의 Id를 userprofile의 id로 사용
	@JoinColumn(name = "user_id")
	private Users user;
	
	
	@Column(nullable = false,length = 100)
	private String name;
	private String phone;
	private Short birth_Year;
	
	@Lob
	private String profile_image_url;
	
	@Column(length = 255)
	private String headline;
	
	@Lob	//큰데이터 저장할때 사용
	private String summary;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_region_id")
	private Integer location_region_id;
	

	
}
