package boot.data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import boot.data.entity.Users;
import boot.data.repository.UsersRepository;

@Service
public class UserService {
	
	@Autowired
	private UsersRepository repository;
	
	public List<Users> getAllLists(){
		return repository.findAll();
	}

}
