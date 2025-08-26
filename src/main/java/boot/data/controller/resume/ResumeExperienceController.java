
package boot.data.controller.resume;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.resume.ExperienceRequest;
import boot.data.dto.resume.ExperienceResponse;
import boot.data.service.ResumeExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ResumeExperienceController {

    private final ResumeExperienceService service;

    @GetMapping("/resumes/{resumeId}/experiences")
    public List<ExperienceResponse> list(@PathVariable("resumeId") Long resumeId) {
        return service.list(resumeId);
    }

    @PostMapping("/resumes/{resumeId}/experiences")
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceResponse create(@PathVariable("resumeId") Long resumeId,
                                     @Valid @RequestBody ExperienceRequest req) {
        return service.create(resumeId, req);
    }

    @PutMapping("/resumes/experiences/{experienceId}")
    public ExperienceResponse update(@PathVariable("experienceId") Long experienceId,
                                     @Valid @RequestBody ExperienceRequest req) {
        return service.update(experienceId, req);
    }

    @DeleteMapping("/resumes/experiences/{experienceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("experienceId") Long experienceId) {
        service.delete(experienceId);
    }
}
