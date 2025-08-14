// package boot.data.controller;

// import java.net.URI;
// import java.util.Map;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import boot.data.dto.ResumeCreateDto;
// import boot.data.service.ResumeService;
// import boot.data.util.AuthUtil;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// //이력서 관련 API컨트롤러
// //개인회원(USER)만 접근 가능하게

// @Slf4j
// @RestController
// @RequestMapping("/resumes")
// @RequiredArgsConstructor
// public class ResumeController {
    
//     private final AuthUtil authUtil;

//     //이력서 생성 API
//     //POST /resumes

// @PostMapping
// public ResponseEntity<?> createResume(@Valid @RequestBody ResumeCreateDto dto){

//     try{
//         log.info("이력서 생성요청: {}", dto);

//         //첫번째 레슨: 개인회원인지 체크
//         authUtil.typeCheckUser();

//         //두번째 레슨:  현재 로그인한 사용자 ID만 추출해서 Service에 전달
//         Long userId = authUtil.getCurrentUserId();
//         log.info("이력서 생성 요청자: userId: ", userId);

//         //세번째 레슨: service에서 처리한 로직 가져오기
//         Long resumeId = ResumeService.createResume(userId,dto);

        // return ResponseEntity.created(URI.create("/api/resumes/" + resumeId))  
//         .body(Map.of("id", resumeId, "message", "이력서가 생성되었습니다"));
        
//     }catch (IllegalStateException e) {
//         log.warn("이력서 작성 권한 없음: {}", e.getMessage());
//         return ResponseEntity.status(403)
//                 .body(Map.of("error", e.getMessage()));
        
//     } catch (Exception e) {
//         log.error("이력서 생성 중 오류 발생", e);
//         return ResponseEntity.status(500)
//                 .body(Map.of("error", "이력서 생성 중 오류가 발생했습니다"));
//     }
// }
// }
