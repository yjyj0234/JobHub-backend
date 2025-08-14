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
    private final CurrentUser currentUser;
    private final UserProfilesRepository userProfilesRepository;

    /** 목록 */
    public List<CommunityCommentDto> getCommentsByPost(Long postId) {
        // 비로그인도 조회 가능하도록 null 허용

       Long me = currentUser.idOrThrow();

       String userName = userProfilesRepository.findByUserId(postId)
                .map(UserProfiles::getName)
                .orElse("탈퇴회원");

        return commentsRepository.findByPost_IdOrderByCreatedAtAsc(postId)
                .stream()
                .map(c -> CommunityCommentDto.builder()
                        .id(c.getId())
                        .postId(c.getPost().getId())
                        .userId(c.getUser().getId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .isOwner(me != null && me.equals(c.getUser().getId()))
                        .userName(userName)
                        .build()
                )
                .toList();
    }

    /** 등록 (작성자 = 현재 로그인 사용자) */
    @Transactional
    public CommunityCommentDto addComment(Long postId, String content) {
        String c = content == null ? "" : content.trim();
    if (c.isEmpty()) {
        throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
    }

    CommunityPosts post = postsRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

    Long uid = currentUser.idOrThrow();
    Users user = usersRepository.findById(uid)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + uid));

    CommunityPostsComments saved = commentsRepository.save(
            CommunityPostsComments.builder()
                    .post(post)
                    .user(user)
                    .content(c) // ← 정리된 값 사용
                    .createdAt(LocalDateTime.now())
                    .build()
    );

    return CommunityCommentDto.builder()
            .id(saved.getId())
            .postId(postId)
            .userId(uid)
            .content(saved.getContent())
            .createdAt(saved.getCreatedAt())
            .updatedAt(saved.getUpdatedAt())
            .isOwner(true)
            .build();
    }

    /** 수정 (작성자만) */
    @Transactional
    public CommunityCommentDto updateComment(Long commentId, String newContent) {
        CommunityPostsComments c = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음: " + commentId));

        Long uid = currentUser.idOrThrow();
        if (!c.getUser().getId().equals(uid)) {
            throw new SecurityException("댓글 수정 권한 없음");
        }

        c.setContent(newContent);
        c.setUpdatedAt(LocalDateTime.now());

        return CommunityCommentDto.builder()
                .id(c.getId())
                .postId(c.getPost().getId())
                .userId(c.getUser().getId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .isOwner(true) // 내 댓글
                .build();
    }

    /** 삭제 (작성자만, DB 한 방으로 원자적) */
    @Transactional
    public void deleteComment(Long commentId) {
        Long uid = currentUser.idOrThrow();
        long deleted = commentsRepository.deleteByIdAndUser_Id(commentId, uid);
        if (deleted == 0) {
            throw new SecurityException("댓글 삭제 권한 없음 또는 존재하지 않는 댓글");
        }
    }
}
