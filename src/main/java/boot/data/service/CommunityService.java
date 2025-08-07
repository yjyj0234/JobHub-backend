package boot.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.entity.CommunityPosts;
import boot.data.entity.Users;
import boot.data.repository.CommunityPostCommentsRepository;
import boot.data.repository.CommunityPostsRepository;
import boot.data.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {
    
    @Autowired
    private final CommunityPostsRepository communityPostsRepository;
    private final CommunityPostCommentsRepository communityPostCommentsRepository;

    @Autowired
    private final UsersRepository usersRepository;

    @Transactional
    public void insertPost()
    {
        
    }
   

}
