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
@EnableWebSecurity
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
            .authorizeHttpRequests(auth -> auth

                // 프리플라이트 요청 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ 공개 경로 여기서 추가하세요 
                .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/ws/**").permitAll()
                .requestMatchers("/public/**", "/docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/search/**",
                    "/jobpostinglist/**",     // 기존 경로 유지
                    "/api/jobpostinglist/**",
                    "/group-chat/rooms",
                    "/api/jobs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/search/**").permitAll()

                 // 🔒 비공개or특수조건공개 -비공개나 역할로공개페이지는 여기서 추가하고 아래처럼 페이지랑 설명 적어주세요
                   // 이력서: USER만 입장가능
                 .requestMatchers("/resumes/**").hasAuthority("USER")

               // company만 입장
               .requestMatchers(HttpMethod.POST, "/api/uploads/**").hasAuthority("COMPANY")
                .requestMatchers(HttpMethod.POST, "/api/postings/**").hasAuthority("COMPANY")
                .requestMatchers(HttpMethod.PUT,  "/api/postings/**").hasAuthority("COMPANY")
                .requestMatchers(HttpMethod.DELETE,"/api/postings/**").hasAuthority("COMPANY")

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
