package boot.data.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "users")
@Entity
public class Users {
	
	   public enum UserType {
	        JOBSEEKER,
	        COMPANY_HR,
	        ADMIN
	    }
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "email")
	private String email;
	
	
	private String password_hash;
	
	@Enumerated(EnumType.STRING)
	@Column(name ="user_type",nullable = false)
	@Comment("사용자 유형(JOBSEEKER/COMPANY_HR/ADMIN)")
	private UserType userType;
	
	private boolean is_Active=true;
	
	private Timestamp email_verfied_at;
	private Timestamp last_login_at;
	
	private LocalDateTime created_At=LocalDateTime.now();
}
