package boot.data.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boot.data.dto.ApplicationCreateRequest;
import boot.data.dto.ApplicationResponse;
import boot.data.entity.Applications;
import boot.data.security.AuthUser;
import boot.data.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationResponse>> listByPostingId(
            @RequestParam Long postingId,
            @AuthenticationPrincipal AuthUser me) {
    
        List<ApplicationResponse> apps = applicationService.findByPostingIdForCompany(me.id(), postingId);
        return ResponseEntity.ok(apps);
    }
    

      // ✅ [신규] 열람 처리 (viewed_at 갱신 + APPLIED → VIEWED)
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    @PatchMapping(value = "/{id}/view")
    public ResponseEntity<Void> markViewed(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser me) {
        applicationService.markViewedByCompany(me.id(), id);
        return ResponseEntity.noContent().build();
    }

    // ✅  상태 변경
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal AuthUser me) {
        String status = body != null ? body.get("status") : null;
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        applicationService.changeStatusByCompany(me.id(), id, status);
        return ResponseEntity.noContent().build();
    }
}