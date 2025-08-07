package boot.data.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import boot.data.type.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "users")
@Entity
public class Users {
	//카멜케이스로 언더바 없이 붙여쓰면 알아서 언더바 있는걸로 인식해줌
	
	  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;
	
	@Enumerated(EnumType.STRING)
	@Column(name ="user_type",nullable = false)
	@Comment("사용자 유형(user/company/admin)")
	private UserType userType;
	
	@Column(name = "is_Active")
	private boolean isActive=true;
	
	@Column(name = "email_verfied_at")
	private Timestamp emailVerifiedAt;
	
	
	private Timestamp lastLoginAt;
	
	private LocalDateTime createdAt=LocalDateTime.now();
	
}
