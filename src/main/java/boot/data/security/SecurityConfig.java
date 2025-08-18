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
                    "/ws/**", "/community/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/search/**",
                    "/jobpostinglist/**",
                    "/api/jobpostinglist/**",
                    "/group-chat/rooms",
                    "/api/jobs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/search/**").permitAll()

                // 2) 회사 전용(공고 등록 페이지/API)
                .requestMatchers("/jobposting", "/jobposting/**").hasAnyAuthority("COMPANY","ADMIN")

                // 3) 이력서: 전부 USER만 접근 (GET 공개 원하면 아래 주석 참고)
                .requestMatchers("/resumes", "/resumes/**").hasAuthority("USER")
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
