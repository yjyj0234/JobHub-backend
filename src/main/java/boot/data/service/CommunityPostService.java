package boot.data.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.CommunityPostDto;
import boot.data.entity.CommunityPosts;
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
public class CommunityPostService {

    private final CommunityPostsRepository postsRepository;
    private final UsersRepository usersRepository;
    private final UserProfilesRepository profilesRepository;
    private final CommunityPostCommentsRepository commentsRepository;
    private final CurrentUser currentUser;

    /* ==========================
       Create
       ========================== */
    @Transactional
    public CommunityPostDto insertDto(CommunityPostDto dto) {
        Long uid = currentUser.idOrThrow();
        Users user = usersRepository.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        LocalDateTime now = LocalDateTime.now();
        CommunityPosts entity = CommunityPosts.builder()
                .title(nullToEmpty(dto.getTitle()))
                .content(nullToEmpty(dto.getContent()))
                .viewCount(0)
                .createdAt(now)
                .updatedAt(now)
                .user(user)
                .build();

        CommunityPosts saved = postsRepository.save(entity);

        String userName = profilesRepository.findByUserId(uid)
                .map(UserProfiles::getName)
                .orElse("탈퇴회원");

        return toDto(saved,
                uid,                  // currentUserId
                0L,                   // commentCount
                userName,             // userName
                true                  // owner
        );
    }

    /* ==========================
       Read One
       ========================== */
    public CommunityPostDto getOne(Long id) {
        CommunityPosts p = postsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

        Long curr = safeCurrentUserId();
        boolean owner = (curr != null) && p.getUser() != null && Objects.equals(curr, p.getUser().getId());

        long commentCount = commentsRepository.countByPost_Id(p.getId());

        String userName = (p.getUser() == null)
                ? "탈퇴회원"
                : profilesRepository.findByUserId(p.getUser().getId())
                    .map(UserProfiles::getName)
                    .orElse("탈퇴회원");

        return toDto(p, curr, commentCount, userName, owner);
    }

    /* ==========================
       Read List (최신순)
       ========================== */
    public List<CommunityPostDto> getList() {
        List<CommunityPosts> posts = postsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        if (posts.isEmpty()) return List.of();

        Long curr = safeCurrentUserId();

        // 배치로 필요한 키 수집
        List<Long> postIds = posts.stream().map(CommunityPosts::getId).toList();
        Set<Long> userIds = posts.stream()
                .map(CommunityPosts::getUser)
                .filter(Objects::nonNull)
                .map(Users::getId)
                .collect(Collectors.toSet());

        // 댓글 수 벌크 집계
        Map<Long, Long> commentCntMap = new HashMap<>();
        for (Object[] row : commentsRepository.countByPostId(postIds)) {
            Long postId = (Long) row[0];
            Long cnt = (Long) row[1];
            commentCntMap.put(postId, cnt);
        }

        // 유저 이름 배치 조회
        Map<Long, String> nameMap = new HashMap<>();
            for (Long uid : userIds) {
            String name = profilesRepository.findByUserId(uid)
            .map(up -> up.getName())
            .orElse("탈퇴회원");
            nameMap.put(uid, name);
        }

        return posts.stream()
                .map(p -> {
                    Long authorId = (p.getUser() != null) ? p.getUser().getId() : null;
                    boolean owner = (curr != null) && authorId != null && curr.equals(authorId);
                    long cmtCnt = commentCntMap.getOrDefault(p.getId(), 0L);
                    String userName = (authorId == null)
                            ? "탈퇴회원"
                            : nameMap.getOrDefault(authorId, "탈퇴회원");
                            
                    return toDto(p, curr, cmtCnt, userName, owner);
                })
                .toList();
    }

    /* ==========================
       Update
       ========================== */
    @Transactional
    public CommunityPostDto update(Long id, CommunityPostDto dto) {
        CommunityPosts p = postsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

        Long uid = currentUser.idOrThrow();
        if (p.getUser() == null || !Objects.equals(p.getUser().getId(), uid)) {
            throw new AccessDeniedException("작성자만 수정 가능");
        }

        p.setTitle(nullToEmpty(dto.getTitle()));
        p.setContent(nullToEmpty(dto.getContent()));
        p.setUpdatedAt(LocalDateTime.now());

        // 저장은 dirty checking
        long commentCount = commentsRepository.countByPost_Id(p.getId());
        String userName = profilesRepository.findByUserId(uid)
                .map(UserProfiles::getName)
                .orElse("탈퇴회원");

        return toDto(p, uid, commentCount, userName, true);
    }

    /* ==========================
       Delete
       ========================== */
    @Transactional
    public void delete(Long id) {
        CommunityPosts p = postsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
        Long uid = currentUser.idOrThrow();
        if (p.getUser() == null || !Objects.equals(p.getUser().getId(), uid)) {
            throw new AccessDeniedException("작성자만 삭제 가능");
        }
        // 댓글도 같이 삭제
        commentsRepository.deleteByPost_Id(p.getId());
        postsRepository.delete(p);
    }

    /* ==========================
       View Count (동시성 고려)
       ========================== */
    @Transactional
    public void increaseViewCount(Long id) {
        // 동시성 안전한 벌크 업데이트 (권장)
        int updated = postsRepository.increaseViewCount(id);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found");
        }

        // 단순 읽고 +1 (동시성 취약)로 하고 싶으면 아래 주석을 사용
        /*
        CommunityPosts p = postsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
        Integer vc = Optional.ofNullable(p.getViewCount()).orElse(0);
        p.setViewCount(vc + 1);
        // flush는 트랜잭션 종료 시점
        */
    }

    /* ==========================
       Helpers
       ========================== */
    private CommunityPostDto toDto(CommunityPosts p,
                                   Long currentUserId,
                                   Long commentCount,
                                   String userName,
                                   boolean owner) {
        return CommunityPostDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .viewCount(Optional.ofNullable(p.getViewCount()).orElse(0))
                .createdAt(Optional.ofNullable(p.getCreatedAt()).orElse(LocalDateTime.now()))
                .updatedAt(Optional.ofNullable(p.getUpdatedAt()).orElse(p.getCreatedAt()))
                .userName(userName)
                .commentCount(commentCount)
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .owner(owner)
                .build();
    }

    private Long safeCurrentUserId() {
        try { return currentUser.idOrThrow(); }
        catch (Exception ignored) { return null; }
    }

    private String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
}
