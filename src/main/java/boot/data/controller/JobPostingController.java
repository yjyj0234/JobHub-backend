package boot.data.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.JobPostingCreateDto;
import boot.data.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping
    public ResponseEntity<CreatePostingResponse> create(@Valid @RequestBody JobPostingCreateDto dto){
        Long id= jobPostingService.create(dto);
        return ResponseEntity
        .created(URI.create("/api/postings/"+id))
        .body(new CreatePostingResponse(id));
    }

    public record CreatePostingResponse(Long id) {}
    

}