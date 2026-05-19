package com.ev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Spring Security 설정 클래스라는 의미
@Configuration
public class SecurityConfig {

    // SecurityFilterChain Bean 등록
    // -> Spring Security의 보안 규칙 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
        
            // CSRF 보안 기능 비활성화
            // 개발 초기 단계에서는 보통 꺼두고 사용
            .csrf(csrf -> csrf.disable())

            // URL 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
            
                // 모든 요청(URL) 허용
                // 현재는 로그인 없이 전체 접근 가능
                .anyRequest().permitAll()
            )

            // Spring Security 기본 로그인 화면 비활성화
            // 기본 "Please sign in" 페이지 제거
            .formLogin(form -> form.disable());

        // 설정 내용 적용 후 반환
        return http.build();
    }
}