package boot.data.dto;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "skills")
public class Skills {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("기술 ID")
	private Integer id;
	
	@Column(nullable = false, length = 100, unique = true)
	private String name;
	
    @Column(name = "category_id")
	private Integer categoryId;
    
    @Column(name = "is_verified", nullable = false)
    @Comment("검증 여부")
    private boolean isverified=false;
}
