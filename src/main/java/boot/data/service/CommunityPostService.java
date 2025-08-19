package boot.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
public class CommunityPostService {

    private final CommunityPostsRepository communityPostsRepository;
    private final UsersRepository usersRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final CurrentUser currentUser; 
    private final CommunityPostCommentsRepository commentsRepositor; // 댓글 수를 가져오기 위한 레포지토리
    // 서비스 레이어에서 현재 로그인 사용자에 접근하기 위한 헬퍼
    // === Create ===
    @Transactional
    public CommunityPostDto insertDto(CommunityPostDto dto) {
        Long id =  currentUser.idOrThrow(); 
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "는 존재하지 않습니다"));

        CommunityPosts post = CommunityPosts.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(Optional.ofNullable(dto.getViewCount()).orElse(0))
                .build();

                post.setCreatedAt(LocalDateTime.now());
                post.setUpdatedAt(LocalDateTime.now());

        CommunityPosts saved = communityPostsRepository.save(post);

        CommunityPostDto res = new CommunityPostDto();
        res.setId(saved.getId());
        res.setUserId(saved.getUser().getId());
        res.setTitle(saved.getTitle());
        res.setContent(saved.getContent());
        res.setViewCount(saved.getViewCount());
        res.setCreatedAt(saved.getCreatedAt());
        res.setUpdatedAt(saved.getUpdatedAt());
        return res;
    }

    // === Read One ===
    @Transactional(readOnly = true)
    public CommunityPostDto getOne(Long id) {
        CommunityPosts p = communityPostsRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found")); // 404로
        
        // 유저 정보 가져오기
        String userName = userProfilesRepository.findByUserId(p.getUser().getId())
                .map(UserProfiles::getName)
                .orElse("탈퇴회원");

                Long currentUserId = null;
                    try {
                        currentUserId = currentUser.idOrThrow(); // 로그인 안 했으면 예외
                    } catch (Exception e) {
                        // 비로그인 사용자는 null 처리
                    }
        
        CommunityPostDto dto = new CommunityPostDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setContent(p.getContent());
        dto.setViewCount(Optional.ofNullable(p.getViewCount()).orElse(0));
        dto.setCreatedAt(Optional.ofNullable(p.getCreatedAt()).orElse(LocalDateTime.now()));
        dto.setUserId(p.getUser() != null ? p.getUser().getId() : null);
        dto.setUserName(userName);
        dto.setOwner(currentUserId != null && currentUserId.equals(p.getUser().getId()));
        return dto;
    }

    // === Read List (최신순) ===
   @Transactional(readOnly = true)
public List<CommunityPostDto> getList() {
    List<CommunityPosts> posts = communityPostsRepository.findAll(
            Sort.by(Sort.Direction.DESC, "id"));
            if (posts.isEmpty()) return Collections.emptyList();

    // 1) 포스트 ID 모으기
    List<Long> postIds = posts.stream().map(CommunityPosts::getId).toList();

    // 2) 댓글 수 한 번에 집계해서 Map으로
    List<Object[]> rows = commentsRepositor.countByPostIdIn(postIds);
    Map<Long, Long> commentCountMap = new HashMap<>();
    for (Object[] row : rows) {
        Long postId = (Long) row[0];
        Long cnt    = (Long) row[1];
        commentCountMap.put(postId, cnt);
    }

    List<CommunityPostDto> result = new ArrayList<>();
    for (CommunityPosts post : posts) {
        Long uid = (post.getUser() != null) ? post.getUser().getId() : null;
        String userName = (uid == null)
                ? "탈퇴회원"
                : userProfilesRepository.findByUserId(uid)
                    .map(UserProfiles::getName)
                    .orElse("탈퇴회원");

        CommunityPostDto dto = new CommunityPostDto();
        dto.setId(post.getId());
        dto.setUserId(uid);
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setViewCount(Optional.ofNullable(post.getViewCount()).orElse(0));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setUserName(userName);
        dto.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L)); // 댓글 수 설정
        result.add(dto);
    }
    return result;
}

    // === Update ===
    @Transactional
    public CommunityPostDto update(Long id, CommunityPostDto dto) {
        CommunityPosts post = communityPostsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + id));

        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        CommunityPostDto res = new CommunityPostDto();
        res.setId(post.getId());
        res.setUserId(post.getUser() != null ? post.getUser().getId() : null);
        res.setTitle(post.getTitle());
        res.setContent(post.getContent());
        res.setViewCount(post.getViewCount());
        res.setUpdatedAt(post.getUpdatedAt());
        return res;
    }

    // === Delete ===
    public void delete(Long id) {
    CommunityPosts post = communityPostsRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("이미 삭제되었거나 존재하지 않음: " + id));

    Long currentUserId = currentUser.idOrThrow(); 
    if (!post.getUser().getId().equals(currentUserId)) {
        throw new AccessDeniedException("본인 글만 삭제 가능");
     }
            communityPostsRepository.delete(post);
        }

    // === View Count 증가 ===
    @Transactional
    public void increaseViewCount(Long id) {
        CommunityPosts post = communityPostsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + id));
        post.setViewCount(post.getViewCount() + 1);
    }
}
