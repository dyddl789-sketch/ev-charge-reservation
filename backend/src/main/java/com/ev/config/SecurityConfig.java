package com.ev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.ev.security.EvLoginFailureHandler;
import com.ev.security.EvLoginSuccessHandler;
import com.ev.security.EvOAuth2UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Spring Security 설정 클래스
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final EvLoginSuccessHandler evLoginSuccessHandler;
    private final EvLoginFailureHandler evLoginFailureHandler;
    private final EvOAuth2UserService evOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // 개발 편의를 위해 CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // URL 접근 권한 설정
            .authorizeHttpRequests(auth -> auth

                // 로그인 없이 접근 가능
                .requestMatchers(
                    "/",
                    "/main",
                    "/login",
                    "/member/join",
                    "/member/find",
                    "/css/**",
                    "/js/**",
                    "/image/**",
                    "/images/**",
                    "/favicon.ico",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                // 관리자만 접근 가능
                // DB user_type = ADMIN 이면 EvUserDetails에서 ROLE_ADMIN 으로 변환되어야 함
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 로그인 사용자만 접근 가능
                .requestMatchers(
                	    "/vehicle/**",
                	    "/reservation/**",
                	    "/station/**",
                	    "/ai-chat/**",
                	    "/member/mypage/**"
                	).authenticated()

                // 나머지는 일단 허용
                .anyRequest().permitAll()
            )

            // 일반 로그인 설정
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("userId")
                .passwordParameter("password")
                .successHandler(evLoginSuccessHandler)
                .failureHandler(evLoginFailureHandler)
                .permitAll()
            )

            // 카카오 OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(evOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    log.info("@# OAuth2 login success");
                    response.sendRedirect("/main");
                })
                .failureHandler((request, response, exception) -> {
                    log.error("@# OAuth2 login fail", exception);
                    response.sendRedirect("/login?error=true");
                })
            )

            // 권한 부족 처리
            // 일반 USER가 /admin/** 접근하면 메인으로 돌려보냄
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(accessDeniedHandler())
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    /*
     * 권한 부족 처리 핸들러
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            log.warn("@# access denied url => {}", request.getRequestURI());
            log.warn("@# access denied message => {}", accessDeniedException.getMessage());

            response.sendRedirect("/main?authMsg=accessDenied");
        };
    }

    /*
     * 비밀번호 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}