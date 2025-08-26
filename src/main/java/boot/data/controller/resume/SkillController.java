
package boot.data.controller.resume;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.SkillCreateRequest;
import boot.data.dto.resume.SkillResponse;
import boot.data.entity.Skills;
import boot.data.service.SkillService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody SkillCreateRequest req) {
        Skills s = skillService.createOrGet(req);
        // FE는 숫자 or {id:...} 모두 수용 → 숫자로 깔끔히 반환
        return ResponseEntity.ok(s.getId());
    }

    // (옵션) 전역 스킬 조회가 필요하면 아래처럼 추가
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getOne(@PathVariable Integer id) {
        // ... repository로 조회 후 매핑
        return ResponseEntity.notFound().build();
    }
}
