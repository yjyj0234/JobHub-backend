package boot.data.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.entity.CommunityPosts;
import boot.data.entity.Users;
import boot.data.repository.UserRepository;
import boot.data.service.CommunityService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/community")
public class CommunityController {
    
    @Autowired
    private CommunityService service;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/addpost")
    public void insertPost(@RequestBody CommunityPosts posts)
    {
        Long userId = posts.getUser().getId();

          Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        
        posts.setUser(user);
        posts.setCreatedAt(LocalDateTime.now());
        posts.setUpdatedAt(LocalDateTime.now());
        posts.setViewCount(0);
        
        service.insertPost(posts);
    }
}
