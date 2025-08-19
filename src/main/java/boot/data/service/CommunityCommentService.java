package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.CommunityCommentDto;
import boot.data.entity.CommunityPosts;
import boot.data.entity.CommunityPostsComments;
import boot.data.entity.UserProfiles;
import boot.data.entity.Users;
import boot.data.repository.CommunityPostCommentsRepository;
import boot.data.repository.CommunityPostsRepository;
import boot.data.repository.UserProfilesRepository;
import boot.data.repository.UsersRepository;
import boot.data.security.CurrentUser;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class CommunityCommentService {

    private final CommunityPostCommentsRepository commentsRepository;
    private final CommunityPostsRepository postsRepository;
    private final UsersRepository usersRepository;
    private final UserProfilesRepository userProfileRepository; 
    private final CurrentUser currentUser;


    // 각 게시글 댓글 조회
    public List<CommunityCommentDto> getCommentsByPost(Long postId) {
        
        return commentsRepository.findByPost_IdOrderByCreatedAtDesc(postId)
                .stream()
                .map(comment -> {
                    String userName = userProfileRepository.findByUserId(comment.getUser().getId())
                            .map(profile -> profile.getName()) // UserProfiles 엔티티에서 이름 가져오기
                            .orElse("알 수 없음");
                            Long currentUserId = null;
                            try {
                                currentUserId = currentUser.idOrThrow();
                            } catch (Exception e) {
                                // 현재 사용자가 로그인하지 않은 경우
                            }



                    return CommunityCommentDto.builder()
                            .id(comment.getId())
                            .postId(comment.getPost().getId())
                            .userId(comment.getUser().getId())
                            .userName(userName)
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .updatedAt(comment.getUpdatedAt())
                            .isOwner(currentUserId != null && comment.getUser().getId().equals(currentUserId))
                            .build();
                })
                .toList();
               
    }

    //댓글 추가 
    @Transactional
    public CommunityCommentDto addComment(Long postId, CommunityCommentDto dto) {
        CommunityPosts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없음: " + postId));
        if (dto == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
        throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        Long userId = currentUser.idOrThrow();
                Users users=   usersRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음: " + userId));

        CommunityPostsComments comment = CommunityPostsComments.builder()
                .post(post)
                .user(users)
                .content(dto.getContent())
                .build();    
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());  
        
        CommunityPostsComments saved = commentsRepository.save(comment);
         String userName = userProfileRepository.findByUserId(saved.getUser().getId())
                .map(profile -> profile.getName())
                .orElse("알 수 없음");

        CommunityCommentDto res = new CommunityCommentDto();
        res.setId(saved.getId());
        res.setPostId(saved.getPost().getId());
        res.setUserId(saved.getUser().getId());
        res.setContent(saved.getContent());
        res.setUserName(userName);
        res.setCreatedAt(saved.getCreatedAt());
        res.setUpdatedAt(saved.getUpdatedAt());
        res.setOwner(true);
        return res;

    }

   

    //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long requesterUserId, boolean isAdmin) {
        CommunityPostsComments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없음: " + commentId));

        if (!isAdmin && !comment.getUser().getId().equals(requesterUserId)) {
            throw new SecurityException("댓글 삭제 권한 없음");
        }
        
        comment.setContent("");
        comment.setUpdatedAt(LocalDateTime.now());
    }
}