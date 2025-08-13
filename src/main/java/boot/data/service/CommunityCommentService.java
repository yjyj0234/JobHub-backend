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

    // 댓글 DTO 변환
    private CommunityCommentDto toDto(CommunityPostsComments comments) {
        String userName = userProfileRepository.findByUserId(comments.getUser().getId())
                .map(profile -> profile.getName()) // UserProfiles 엔티티에서 이름 가져오기
                .orElse("알 수 없음");

                 // ✅ 로그인 안 한 경우도 대비 (idOrNull 만들었다고 가정, 없으면 try/catch로 감싸)
            Long me = null;
            try { me = currentUser.idOrThrow(); } catch (Exception ignore) {}

            boolean owner = (me != null) && me.equals(comments.getUser().getId());

    // ✅ 권한 체크 (프로젝트 정책에 맞게 보완)
    String role = currentUser.roleOrNull(); // 예: "ADMIN", "USER", "ROLE_ADMIN"
    boolean admin = role != null && (role.equals("ADMIN"));

        return CommunityCommentDto.builder()
                .id(comments.getId())
                .postId(comments.getPost().getId())
                .userId(comments.getUser().getId())
                .userName(userName)
                .content(comments.isDeleted() ? "삭제된 댓글입니다." : comments.getContent())
                .isDeleted(comments.isDeleted())
                .createdAt(comments.getCreatedAt())
                .updatedAt(comments.getUpdatedAt())
                .isOwner(owner)   // ✅ 여기서 내려줌
                .isAdmin(admin)
                .build();
    }

    // 각 게시글 댓글 조회
    public List<CommunityCommentDto> getCommentsByPost(Long postId) {
        return commentsRepository.findByPost_IdAndIsDeletedFalseOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    //댓글 추가 
    @Transactional
    public CommunityCommentDto addComment(Long postId, String content) {
        CommunityPosts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없음: " + postId));

        Long userid = currentUser.idOrThrow();        
        Users user = usersRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음: " + userid));

        CommunityPostsComments saved = commentsRepository.save(
                CommunityPostsComments.builder()
                        .post(post)
                        .user(user)
                        .content(content)
                        .isDeleted(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        return toDto(saved);
    }

    //댓글 수정
    @Transactional
    public CommunityCommentDto updateComment(Long commentId, String newContent) {
        CommunityPostsComments comment = commentsRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없음: " + commentId));

        Long editorUserId = currentUser.idOrThrow();
        if (!comment.getUser().getId().equals(editorUserId)) {
            throw new SecurityException("댓글 수정 권한 없음");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return toDto(comment);
    }

    //댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        CommunityPostsComments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없음: " + commentId));
        Long requesterUserId = currentUser.idOrThrow();
        boolean isAdmin = currentUser.roleOrNull().equals("ADMIN");
        if (!isAdmin && !comment.getUser().getId().equals(requesterUserId)) {
            throw new SecurityException("댓글 삭제 권한 없음");
        }

        comment.setDeleted(true);
        comment.setContent("");
        comment.setUpdatedAt(LocalDateTime.now());
    }
}
