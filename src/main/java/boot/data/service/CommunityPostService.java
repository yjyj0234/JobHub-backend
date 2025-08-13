package boot.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.CommunityPostDto;
import boot.data.entity.CommunityPosts;
import boot.data.entity.UserProfiles;
import boot.data.entity.Users;
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

        CommunityPostDto dto = new CommunityPostDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setContent(p.getContent());
        dto.setViewCount(Optional.ofNullable(p.getViewCount()).orElse(0));
        dto.setCreatedAt(Optional.ofNullable(p.getCreatedAt()).orElse(LocalDateTime.now()));
        dto.setUserName(userName);
        return dto;
    }

    // === Read List (최신순) ===
   @Transactional(readOnly = true)
public List<CommunityPostDto> getList() {
    List<CommunityPosts> posts = communityPostsRepository.findAll(
            Sort.by(Sort.Direction.DESC, "id"));

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

        CommunityPostDto res = new CommunityPostDto();
        res.setId(post.getId());
        res.setUserId(post.getUser() != null ? post.getUser().getId() : null);
        res.setTitle(post.getTitle());
        res.setContent(post.getContent());
        res.setViewCount(post.getViewCount());
        res.setCreatedAt(post.getCreatedAt());
        res.setUpdatedAt(post.getUpdatedAt());
        return res;
    }

    // === Delete ===
    @Transactional
    public void delete(Long id) {
        if (!communityPostsRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 삭제되었거나 존재하지 않음: " + id);
        }
        communityPostsRepository.deleteById(id);
    }

    // === View Count 증가 ===
    @Transactional
    public void increaseViewCount(Long id) {
        CommunityPosts post = communityPostsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + id));
        post.setViewCount(post.getViewCount() + 1);
    }
}
