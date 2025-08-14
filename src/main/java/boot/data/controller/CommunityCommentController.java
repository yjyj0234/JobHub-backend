package boot.data.controller;




import org.springframework.web.bind.annotation.*;

import boot.data.service.CommunityCommentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/community/comments")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityCommentService commentService;
       
    
}
