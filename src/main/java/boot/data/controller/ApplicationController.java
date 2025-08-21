package boot.data.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.ApplicationCreateRequest;
import boot.data.dto.ApplicationResponse;
import boot.data.entity.Applications;
import boot.data.security.AuthUser;
import boot.data.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController  {

    private final ApplicationService applicationService;

    // 프론트가 FormData로 보내므로 multipart/form-data 수신
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationResponse> Create(@Valid @ModelAttribute ApplicationCreateRequest req,
                                @AuthenticationPrincipal AuthUser me) {
        
        Applications saved=applicationService.apply(me.id(), req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApplicationResponse.from(saved));
    }
    
     // === 2) JSON으로 보낼 때(옵션) ===
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApplicationResponse> createFromJson(
            @Valid @RequestBody ApplicationCreateRequest req,
            @AuthenticationPrincipal AuthUser me) {

        Applications saved = applicationService.apply(me.id(), req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApplicationResponse.from(saved));
    }
}