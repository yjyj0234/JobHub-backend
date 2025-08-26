package boot.data.controller;

import java.beans.PropertyEditorSupport;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import boot.data.dto.UserDto;
import boot.data.entity.Users;
import boot.data.jwt.JwtTokenProvider;
import boot.data.repository.UsersRepository;
import boot.data.security.AuthUser;
import boot.data.type.UserType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsersRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UserType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null) {
                    setValue(null);
                } else {
                    String normalized = text.trim().toUpperCase(Locale.ROOT);
                    setValue(UserType.valueOf(normalized));
                }
            }
        });
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginDto) {
    var userOpt = userRepository.findByEmail(loginDto.getEmail());

    // 1) 이메일 미존재
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "code", "USER_NOT_FOUND",
                    "message", "가입되지 않은 이메일입니다."
                ));
    }

    Users user = userOpt.get();

    // 2) 비밀번호 불일치
    if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "code", "WRONG_PASSWORD",
                    "message", "비밀번호가 올바르지 않습니다."
                ));
    }

    // 3) 성공 → JWT 쿠키 발급
    String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getUserType().toString());

    ResponseCookie cookie = ResponseCookie.from("JWT", token)
            .httpOnly(true)
            .secure(false)      // HTTPS면 true
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofHours(1))
            .build();

    Map<String, Object> body = new HashMap<>();
    body.put("email", user.getEmail());
    body.put("role", user.getUserType());
    body.put("id", user.getId());

    ResponseCookie jwt = ResponseCookie.from("JWT", token)
    .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(Duration.ofHours(1)).build();

ResponseCookie mark = ResponseCookie.from("JWT_MARK", "1")
    .httpOnly(false).secure(false).sameSite("Lax").path("/").maxAge(Duration.ofHours(1)).build();

return ResponseEntity.ok()
    .header(HttpHeaders.SET_COOKIE, jwt.toString(), mark.toString())
    .body(body);
}
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 쿠키 제거(만료)
        ResponseCookie expired = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax") // 프론트 POST가 다른 포트면 Lax/None 조정
                .maxAge(0)
                .build();
                ResponseCookie jwtExpired = ResponseCookie.from("JWT", "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(0).build();
            
            ResponseCookie markExpired = ResponseCookie.from("JWT_MARK", "")
                .httpOnly(false).secure(false).sameSite("Lax").path("/").maxAge(0).build();
            
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtExpired.toString(), markExpired.toString())
                .body("ok");
    }
// @PostMapping("/me")
// public Map<String, Object> me(Authentication authentication) {
//     Long uid = (Long) authentication.getPrincipal(); // 필터에서 principal을 uid로 세팅했다는 전제
//     return Map.of("id", uid);
// }
@GetMapping("/me")
public ResponseEntity<UserDto> me(@AuthenticationPrincipal AuthUser me) {
    if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    return userRepository.findById(me.id())
        .map(UserDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
}


@PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
public ResponseEntity<?> register(@ModelAttribute RegisterForm request) {
  try {
    log.info("[/auth/register] email={}, userType={}, name={}, bizRegNo={}, file={}",
        request.getEmail(),
        request.getUserType(),
        request.getName(),
        request.getBusinessRegistrationNumber(),
        request.getBusinessCertificationFile() != null
            ? request.getBusinessCertificationFile().getOriginalFilename()
            : null);

    if (userRepository.existsByEmail(request.getEmail())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 이메일입니다");
    }
    if (request.getUserType() == null) {
      return ResponseEntity.badRequest().body("userType은 필수입니다 (USER 또는 COMPANY)");
    }
    if (request.getEmail() == null || request.getPassword() == null) {
      return ResponseEntity.badRequest().body("email/password는 필수입니다");
    }

    Users user = new Users();
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setUserType(request.getUserType());
    userRepository.save(user);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "회원가입 성공");
    response.put("email", user.getEmail());
    response.put("userType", user.getUserType().name());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  } catch (IllegalArgumentException e) {
    log.error("Register BAD_REQUEST", e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("요청 파싱 오류(IllegalArgument): " + e.getMessage());
  } catch (Exception e) {
    log.error("Register BAD_REQUEST", e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("요청 파싱 오류(" + e.getClass().getSimpleName() + "): " + e.getMessage());
  }
}

    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class RegisterForm {
        private String email;
        private String password;
        private String name;
        private UserType userType;

        private String companyName;
        private String businessRegistrationNumber;
        private MultipartFile businessCertificationFile;
    }
}
