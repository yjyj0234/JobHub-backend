package boot.data.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import boot.data.entity.Users;
import boot.data.repository.UsersRepository;
import boot.data.type.UserType;
import lombok.RequiredArgsConstructor;


//간단한 JWT 인증 정보 추출 유틸리디
//복잡한 비즈니스 로직은 각 service에서 처리합시다. ㅎㅎ
@Component
@RequiredArgsConstructor
public class AuthUtil {
    
    private final UsersRepository usersRepository;

    //현재 로그인한 사용자의 메일을 가져오기
    public String getCurrentUserEmail (){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            return authentication.getName();
        }
        return null;
    }

    //현재 로그인한 사용자의 전체 정보
    public Users getCurrentUser(){

        String email = getCurrentUserEmail();
        if(email != null){
            return usersRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    //현재 로그인 한 사용자 ID 가져올때
    public Long getCurrentUserId(){
        Users user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    //현재 로그인한 사용자가 개인회원인지 간단히 체크
    public void typeCheckUser(){
        Users user = getCurrentUser();
        if(user == null || user.getUserType() != UserType.USER){
            throw new IllegalStateException("이력서 작성은 개인회원만 가능합니다");
        }
    }
    
    //기업회원인지 체크
    public void typeCheckCompany(){
        Users user =getCurrentUser();

        if(user ==null || user.getUserType() != UserType.COMPANY){
            throw new IllegalStateException("채용공고 작성은 기업회원만 가능합니다");
        }
    }
}
