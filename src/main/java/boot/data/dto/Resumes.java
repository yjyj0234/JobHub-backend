package boot.data.dto;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Resumes {
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Comment("이력서 고유 ID")
	    private Long id;

	    // 'user_id' 컬럼과 매핑되는 관계 설정
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "user_id", nullable = false)
	    @Comment("사용자 ID")
	    private Users user;

	    @Column(nullable = false)
	    @Comment("이력서 제목")
	    private String title;

	    @Column(name = "is_primary", nullable = false)
	    @Comment("대표 이력서 여부")
	    private boolean isPrimary = false;

	    @Column(name = "is_public", nullable = false)
	    @Comment("공개 여부")
	    private boolean isPublic = false;

	    @Column(name = "completion_rate", nullable = false)
	    @Comment("완성도 (%)")
	    private short completionRate = 0;
	
	
}
