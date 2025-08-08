package boot.data.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import boot.data.dto.CommunityPostDto;
import boot.data.service.CommunityPostService;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService service;

    //게시판 글쓰기
    @PostMapping("/addpost")
    public ResponseEntity<CommunityPostDto> insertPost(@RequestBody CommunityPostDto dto) {
        CommunityPostDto saved = service.insertDto(dto);
        return ResponseEntity.ok(saved);
    }

    // === 단건 조회 ===
    @GetMapping("/dd")
    public ResponseEntity<CommunityPostDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    //리스트 출력
    @GetMapping("/list")
    public ResponseEntity<List<CommunityPostDto>> getPostList() {
        return ResponseEntity.ok(service.getList());
    }

    // === 수정 ===
    @PutMapping("/")
    public ResponseEntity<CommunityPostDto> updatePost(@PathVariable Long id,
                                                       @RequestBody CommunityPostDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // === 삭제 ===
    @DeleteMapping("/")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === 조회수 증가 ===
    @PostMapping("/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        service.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}
