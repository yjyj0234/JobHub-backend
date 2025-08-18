package boot.data.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.CommunityPostDto;
import boot.data.security.AuthUser;
import boot.data.service.CommunityPostService;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService service;

    //게시판 글쓰기
    @PostMapping("/addpost")
    public ResponseEntity<CommunityPostDto> insertPost(@RequestBody CommunityPostDto dto,
                                                        @AuthenticationPrincipal AuthUser authUser) {
        CommunityPostDto saved = service.insertDto(dto);
        return ResponseEntity.ok(saved);
    }

    // === 단건 조회 ===
    @GetMapping("/detail/{id}")
    public ResponseEntity<CommunityPostDto> getPost(@PathVariable("id") Long id) {  //PathVariable:  요청 URL 경로에 포함된 값을 메서드 파라미터로 바로 매핑해 주는 스프링 MVC 기능
        return ResponseEntity.ok(service.getOne(id));                          // ex) 클라이언트에서 id=18을 보내면 {id=18}로 자동들어감
    }

    //리스트 출력
    @GetMapping("/list")
    public ResponseEntity<List<CommunityPostDto>> getPostList() {
        return ResponseEntity.ok(service.getList());
    }

    // === 수정 ===
    @PutMapping("/edit/{id}") // @AuthenticationPrincipal CustomUser user  // 토큰 쓰면 이걸로 작성자 확인
    public ResponseEntity<CommunityPostDto> updatePost(@PathVariable("id") Long id,
                                                        @RequestBody CommunityPostDto dto
                                                        ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // === 삭제 ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === 조회수 증가 ===
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable("id") Long id) {
        service.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}
