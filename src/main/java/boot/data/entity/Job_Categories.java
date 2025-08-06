package boot.data.entity;

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
@Entity
@Table(name = "job_categories")
public class Job_Categories {
	
	@Id
	@Comment("직무 코드")
	private Integer id;
	
	@Column(nullable = false,length = 100)
	@Comment("직무명")
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@Comment("상위 직무 분류")
	private Job_Categories parent;
	
	@Column(nullable = false)
	private Short level;
	
}
