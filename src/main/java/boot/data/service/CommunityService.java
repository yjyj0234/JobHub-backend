package boot.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import boot.data.entity.CommunityPosts;
import boot.data.entity.Users;
import boot.data.repository.CommunityRepository;
import boot.data.repository.UsersRepository;

@Service
public class CommunityService {
    
    @Autowired
    private CommunityRepository repository;

    @Autowired
    private UsersRepository usersRepository;

    public void insertPost(CommunityPosts posts)
    {
        Long userId = posts.getUser().getId();

          Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        
        posts.setUser(user);
        posts.setCreatedAt(LocalDateTime.now());
        posts.setUpdatedAt(LocalDateTime.now());
        posts.setViewCount(0);
        repository.save(posts);
    }

}
