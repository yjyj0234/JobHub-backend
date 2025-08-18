package boot.data.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.JobDetailResponseDto;
import boot.data.service.JobDetailService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(
  origins = {"http://localhost:5173", "http://localhost:3000"},
  allowCredentials = "true"
)
public class JobListController {
    
        private final JobDetailService jobDetailService;

   //상세조회(뷰카운트+1 포함)
   @GetMapping("/{id}")
  public ResponseEntity<JobDetailResponseDto> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobDetailService.getDetail(id));
    }
   
}
