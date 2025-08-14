package boot.data.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
@EnableMethodSecurity(prePostEnabled = true) // <= 추가
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
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll() // CORS
                .requestMatchers(HttpMethod.GET, "/api/search/**","/jobpostinglist/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/search/**").permitAll()
                .requestMatchers("/auth/**","/public/**").permitAll()

                // ⭐ 검색 전용 엔드포인트는 모든 HTTP 메서드에서 공개 (GET/POST 외 확장 대비)
                .requestMatchers("/api/search/**").permitAll()

                .anyRequest().authenticated()
            )

            // ⭐ 예외 처리: 왜 403/401이 났는지 로그/응답을 명확히 보기 위함 (선택이지만 디버깅 도움 큼)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    // 인증이 필요한데(401) 토큰 없거나 잘못된 경우
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    // 권한 부족(403)
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"error\":\"forbidden\",\"message\":\"" + e.getMessage() + "\"}");
                })
            )

            // ⭐ JWT 필터주의:
            //    - JwtAuthenticationFilter는 Authorization 헤더가 *없으면 그냥 체인 계속 진행* 해야 함
            //    - 공개 경로(/api/search/** 등)는 필터 내부에서 굳이 인증 시도/예외 던지지 않도록 구현 필요
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173" // ⭐ Vite 개발 포트 허용
        ));
        config.setAllowedMethods(List.of(
            "GET","POST","PUT","DELETE","PATCH","OPTIONS" // ⭐ PATCH 포함
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // ⭐ preflight 캐시

        // ⭐ 토큰을 프론트에서 읽어야 한다면(필요 시)
        config.setExposedHeaders(List.of("Authorization"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
