package boot.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.CommunityCommentDto;
import boot.data.entity.CommunityPosts;
import boot.data.entity.CommunityPostsComments;
import boot.data.entity.Users;
import boot.data.repository.CommunityPostCommentsRepository;
import boot.data.repository.CommunityPostsRepository;
import boot.data.repository.UserProfilesRepository;
import boot.data.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class CommunityCommentService {

    private final CommunityPostCommentsRepository commentsRepository;
    private final CommunityPostsRepository postsRepository;
    private final UsersRepository usersRepository;
    private final UserProfilesRepository userProfileRepository; 

    // 댓글 DTO 변환
    private CommunityCommentDto toDto(CommunityPostsComments comments) {
        String userName = userProfileRepository.findByUserId(comments.getUser().getId())
                .map(profile -> profile.getName()) // UserProfiles 엔티티에서 이름 가져오기
                .orElse("알 수 없음");

        return CommunityCommentDto.builder()
                .id(comments.getId())
                .postId(comments.getPost().getId())
                .userId(comments.getUser().getId())
                .userName(userName)
                .createdAt(comments.getCreatedAt())
                .updatedAt(comments.getUpdatedAt())
                .build();
    }

    // 각 게시글 댓글 조회
    public List<CommunityCommentDto> getCommentsByPost(Long postId) {
        return commentsRepository.findByPost_IdAndOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    //댓글 추가 
    @Transactional
    public CommunityCommentDto addComment(Long postId, Long userId, String content) {
        CommunityPosts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없음: " + postId));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음: " + userId));

        CommunityPostsComments saved = commentsRepository.save(
                CommunityPostsComments.builder()
                        .post(post)
                        .user(user)
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        return toDto(saved);
    }

    //댓글 수정
    @Transactional
    public CommunityCommentDto updateComment(Long commentId, Long editorUserId, String newContent) {
        CommunityPostsComments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없음: " + commentId));

        if (!comment.getUser().getId().equals(editorUserId)) {
            throw new SecurityException("댓글 수정 권한 없음");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return toDto(comment);
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
