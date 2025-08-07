package boot.data.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boot.data.entity.UserProfiles;
import boot.data.entity.Users;
import boot.data.jwt.JwtTokenProvider;
import boot.data.repository.UsersRepository;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginDto) {
        Users user = userRepository.findByEmail(loginDto.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 틀렸습니다");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getUserType().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("role", user.getUserType());
        response.put("userId", user.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 이메일입니다");
        }
        
        UserProfiles userProfile = new UserProfiles();
        Users user = new Users();
        user.setEmail(request.getEmail());
        userProfile.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
        private Users.UserType userType; // enum 타입 맞게 조정
    }
}
