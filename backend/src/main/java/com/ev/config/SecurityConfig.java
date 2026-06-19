package com.ev.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ev.security.EvOAuth2UserService;
import com.ev.security.EvJwtAuthenticationFilter;
import com.ev.security.EvOAuth2JwtFailureHandler;
import com.ev.security.EvOAuth2JwtSuccessHandler;
import com.ev.security.EvRestAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final EvJwtAuthenticationFilter jwtAuthenticationFilter;
    private final EvRestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final EvOAuth2UserService evOAuth2UserService;
    private final EvOAuth2JwtSuccessHandler oAuth2JwtSuccessHandler;
    private final EvOAuth2JwtFailureHandler oAuth2JwtFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("@# SecurityConfig.filterChain()");

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .authorizeHttpRequests(auth -> auth
                // 정적 리소스 / 기존 JSP 화면 / OAuth2 진입 허용
                .requestMatchers(
                    "/",
                    "/main",
                    "/login",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/image/**",
                    "/images/**",
                    "/upload/**",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                // React 인증 API
                .requestMatchers("/auth/**").permitAll()

                // 회원가입 / 중복체크는 로그인 없이 허용
                .requestMatchers(HttpMethod.POST, "/member/join").permitAll()
                .requestMatchers("/member/check/**").permitAll()
                .requestMatchers("/member/find/**").permitAll()

                // 공지 / FAQ는 공개 API
                .requestMatchers("/notice/**", "/faq/**").permitAll()

                // 관리자 MIS API 접근 권한
                .requestMatchers("/admin/**")
                    .hasAnyRole("ADMIN", "MANAGER", "OPERATOR", "ENGINEER")

                // 사용자 로그인 필요 API
                .requestMatchers(
                    "/member/mypage/**",
                    "/vehicle/**",
                    "/reservation/**",
                    "/ai-chat/**",
                    "/complaint/**",
                    "/location/**"
                ).authenticated()

                // 충전소 조회는 공개, 등록/수정은 추후 admin에서 처리
                .requestMatchers(HttpMethod.GET, "/station/**").permitAll()

                // 나머지 API는 일단 허용
                .anyRequest().permitAll()
            )

            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorization")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/login/oauth2/code/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(evOAuth2UserService)
                )
                .successHandler(oAuth2JwtSuccessHandler)
                .failureHandler(oAuth2JwtFailureHandler)
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler())
            )

            .logout(logout -> logout.disable())

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("@# access denied url => {}", request.getRequestURI());
            log.warn("@# access denied message => {}", accessDeniedException.getMessage());

            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"권한이 없습니다.\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
