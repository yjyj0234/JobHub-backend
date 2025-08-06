package boot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import boot.data.entity.Users;


public interface UserRepository extends JpaRepository<Users, Long>{
	//interface로 만들것!!!!
	//UserService에서 기존 UserDao 대신 새로 만든 UserRepository를 사용
	
	
}
