package boot.data.controller;

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

    @PostMapping("/addpost")
    public void insertPost(@RequestBody CommunityPosts posts)
    {
        
        
        service.insertPost(posts);
    }
}
