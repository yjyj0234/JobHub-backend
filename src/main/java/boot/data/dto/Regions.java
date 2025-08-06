package boot.data.dto;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "regions")
@Entity
public class Regions {
	@Id
	private Integer id;
	
	@Column(nullable = false,length = 100)
	private String name;
	
	//상위지역코드를 참조하는 관계 설정
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Regions parent;
	
	//tinyint=>java에서 short
	@Column(nullable = false)
    @Comment("계층 레벨 (1=시도, 2=시군구)")
	private Short level;
	
}
