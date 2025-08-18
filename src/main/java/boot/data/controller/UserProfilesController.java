package boot.data.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import boot.data.dto.UserProfilesDto;
import boot.data.security.AuthUser;
import boot.data.service.UserProfilesService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfilesController {

    private final UserProfilesService service;

    @GetMapping("/me")
    public UserProfilesDto me(@AuthenticationPrincipal AuthUser me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return service.getOrDefault(me.id());
    }

    @GetMapping("/{userId}")
public UserProfilesDto get(@PathVariable("userId") Long userId,  // ← 이름 명시
                           @AuthenticationPrincipal AuthUser me) {
    if (me == null || !me.id().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return service.getOrDefault(userId);
}

@PutMapping("/{userId}")
public UserProfilesDto update(@PathVariable("userId") Long userId, // ← 이름 명시
                              @AuthenticationPrincipal AuthUser me,
                              @RequestBody UserProfilesDto dto) {
    if (me == null || !me.id().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return service.upsert(userId, dto);
}

    // @PutMapping("/me")
public UserProfilesDto updateMe(@AuthenticationPrincipal AuthUser me,
@RequestBody /*@Valid*/ UserProfilesDto dto) {
if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
return service.upsert(me.id(), dto);
}

}
