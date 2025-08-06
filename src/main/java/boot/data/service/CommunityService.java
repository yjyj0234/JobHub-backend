package boot.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import boot.data.repository.CommunityRepository;

@Service
public class CommunityService {
    
    @Autowired
    private CommunityRepository repository;

    public void insertPost()
    {
        
    }
}
