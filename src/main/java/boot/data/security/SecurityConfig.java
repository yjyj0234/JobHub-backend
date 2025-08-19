package boot.data.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import boot.data.jwt.JwtAuthenticationFilter;
import boot.data.jwt.JwtTokenProvider;

@Configuration
@EnableWebSecurity(debug = true) // 필터·매처 흐름 상세 로그 출력
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider) {
        return new JwtAuthenticationFilter(provider);
    }


//주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여주석읽어주세여
//한다미치게한다미치게한다미치게한다미치게한다미치게한다미치게한다미치게한다미치게

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(JsonSecurityHandlers.authenticationEntryPoint())
                .accessDeniedHandler(JsonSecurityHandlers.accessDeniedHandler())
            )
            .authorizeHttpRequests(auth -> auth
                // 0) 프리플라이트
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 1) 공개 엔드포인트
                .requestMatchers(
                    "/auth/login", "/auth/register", "/auth/refresh",
                    "/public/**", "/docs/**", "/swagger-ui/**", "/v3/api-docs/**",
                    "/ws/**", "/community/**", "/group-chat/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/search/**",
                    "/jobpostinglist/**",
                    "/api/jobpostinglist/**",
                    "/group-chat/rooms",
                    "/api/jobs/**",
                    "/api/files/view",
                    "/community/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/search/**","/api/uploads/**").permitAll()


               // company만 입장
                .requestMatchers(HttpMethod.POST, "/api/postings/**").hasAuthority("COMPANY")
                .requestMatchers(HttpMethod.PUT,  "/api/postings/**").hasAuthority("COMPANY")
                .requestMatchers(HttpMethod.DELETE,"/api/postings/**").hasAuthority("COMPANY")


                //디버그 on
                .requestMatchers("/error").permitAll()
                // 2) 회사 전용(공고 등록 페이지/API)
                .requestMatchers("/jobposting/**","/api/jobposting/**").hasAnyAuthority("COMPANY","ADMIN")

                // 3) 이력서: 전부 USER만 접근 (GET 공개 원하면 아래 주석 참고)
                // 프로필: 본인 조회/수정만 허용(컨트롤러 @PreAuthorize로 소유자 검사 권장)
                .requestMatchers(HttpMethod.GET,  "/api/profile/**").hasAuthority("USER")
                .requestMatchers(HttpMethod.PUT,  "/api/profile/**").hasAuthority("USER")
                 // 이력서 *****순서 중요: 더 구체적인 permitAll이 먼저 와야 합니다.
                 .requestMatchers(HttpMethod.GET, "/api/resumes/public/**").permitAll()
                 .requestMatchers(HttpMethod.GET, "/api/resumes/**").hasAuthority("USER")
                 .requestMatchers(HttpMethod.POST, "/api/resumes/**").hasAuthority("USER")
                 .requestMatchers(HttpMethod.PUT,  "/api/resumes/**").hasAuthority("USER")
                 .requestMatchers(HttpMethod.DELETE,"/api/resumes/**").hasAuthority("USER")
                // ※ 만약 GET만 공개하고 싶다면 위 한 줄을 지우고 아래 두 줄로 교체
                // .requestMatchers(HttpMethod.GET, "/resumes/**").permitAll()
                // .requestMatchers("/resumes", "/resumes/**").hasAuthority("USER")

                // 4) 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowCredentials(true); // 쿠키 허용
        c.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
        ));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("Set-Cookie")); // 쿠키 기반 인증 시 편의

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
