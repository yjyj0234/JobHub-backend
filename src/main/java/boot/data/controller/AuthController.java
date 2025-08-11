package boot.data.controller;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import boot.data.entity.UserProfiles;
import boot.data.entity.Users;
import boot.data.jwt.JwtTokenProvider;
import boot.data.repository.UsersRepository;
import boot.data.type.UserType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
public ResponseEntity<?> login(@RequestBody LoginRequest loginDto, HttpServletResponse response) {
    Users user = userRepository.findByEmail(loginDto.getEmail()).orElse(null);
    if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 틀렸습니다");
    }

    String token = jwtTokenProvider.createToken(user.getEmail(), user.getUserType().toString());

    // HttpOnly 쿠키로 내려줌 (개발용: SameSite=Lax, https가 아니면 Secure 미설정)
    ResponseCookie cookie = ResponseCookie.from("JWT", token)
            .httpOnly(true)
            .secure(false)               // 운영에서 https면 true
            .sameSite("Lax")             // 운영에서 cross-site 필요하면 "None" + secure(true)
            .path("/")
            .maxAge(60 * 60)             // 1h
            .build();
    response.addHeader("Set-Cookie", cookie.toString());

    Map<String, Object> body = new HashMap<>();
    body.put("email", user.getEmail());
    body.put("role", user.getUserType());
    body.put("userId", user.getId());

    return ResponseEntity.ok(body);
}

@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("JWT", "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(0) // 삭제
            .build();
    response.addHeader("Set-Cookie", cookie.toString());
    return ResponseEntity.ok("로그아웃 완료");
}

    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> register(@ModelAttribute RegisterForm request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 이메일입니다");
            }


            if (request.getUserType() == null) {
                return ResponseEntity.badRequest().body("userType은 필수입니다");
            }

  
            UserProfiles userProfile = new UserProfiles();
            userProfile.setName(request.getName());


            // 유저 생성
            Users user = new Users();
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setUserType(request.getUserType());

    
                System.out.println("여기까지성공");
       
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "회원가입 성공");
            response.put("email", user.getEmail());
            response.put("userType", user.getUserType().name());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 사용자 유형입니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 파싱 오류: " + e.getMessage());
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
