// package boot.data.controller;

// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;

// import boot.data.entity.UserProfiles;
// import boot.data.entity.Users;
// import boot.data.jwt.JwtTokenProvider;
// import boot.data.repository.UsersRepository;
// import boot.data.type.UserType;
// import lombok.Getter;
// import lombok.RequiredArgsConstructor;
// import lombok.Setter;

// @RestController
// @RequestMapping("/auth")
// @RequiredArgsConstructor
// public class AuthController {

//     private final UsersRepository userRepository;
//     private final JwtTokenProvider jwtTokenProvider;
//     private final PasswordEncoder passwordEncoder;

//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody LoginRequest loginDto) {
//         Users user = userRepository.findByEmail(loginDto.getEmail()).orElse(null);
//         if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 틀렸습니다");
//         }

//         String token = jwtTokenProvider.createToken(user.getEmail(), user.getUserType().toString());

//         Map<String, Object> response = new HashMap<>();
//         response.put("token", token);
//         response.put("email", user.getEmail());
//         response.put("role", user.getUserType());
//         response.put("userId", user.getId());

//         return ResponseEntity.ok(response);
//     }

//     @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
//     public ResponseEntity<?> register(@ModelAttribute RegisterForm request) {
//         try {
//             if (userRepository.existsByEmail(request.getEmail())) {
//                 return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 이메일입니다");
//             }

//             UserProfiles userProfile = new UserProfiles();
//             userProfile.setName(request.getName());

//             Users user = new Users();
//             user.setEmail(request.getEmail());
//             user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

//             String userTypeStr = request.getUserType();
//             if (userTypeStr == null || userTypeStr.isEmpty()) {
//                 return ResponseEntity.badRequest().body("userType은 필수입니다");
//             }

//             try {
//                 user.setUserType(UserType.valueOf(userTypeStr.toLowerCase()));
//             } catch (IllegalArgumentException e) {
//                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 사용자 유형입니다: " + userTypeStr);
//             }

//             userRepository.save(user);

//             Map<String, Object> response = new HashMap<>();
//             response.put("message", "회원가입 성공");
//             response.put("email", user.getEmail());
//             response.put("userType", user.getUserType());

//             return ResponseEntity.status(HttpStatus.CREATED).body(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("요청 파싱 오류: " + e.getMessage());
//         }
//     }

//     @Getter
//     @Setter
//     public static class LoginRequest {
//         private String email;
//         private String password;
//     }

//     @Getter
//     @Setter
//     public static class RegisterForm {
//         private String email;
//         private String password;
//         private String name;
//         private UserType userType; // enum 타입 맞게 조정
//         private String companyName;
//         private String businessRegistrationNumber;
//         private MultipartFile businessCertificationFile;
//     }
// }
