package boot.data.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.JobPostingCreateDto;
import boot.data.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    //PreAuthorize : 현재 인증된 사용자가 다음 중 하나라도 역할을 가지고 있으면 메서드 호출을 허용
    @PreAuthorize("hasAnyAuthority('COMPANY')")  
    @PostMapping
    public ResponseEntity<CreatePostingResponse> create(@Valid @RequestBody JobPostingCreateDto dto){
        Long id= jobPostingService.create(dto);
        return ResponseEntity
        .created(URI.create("/api/postings/"+id))
        .body(new CreatePostingResponse(id));
    }

    public record CreatePostingResponse(Long id) {}
    

}