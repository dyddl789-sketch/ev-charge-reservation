package com.ev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.ev.security.EvLoginFailureHandler;
import com.ev.security.EvLoginSuccessHandler;
import com.ev.security.EvOAuth2UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring Security 설정 클래스라는 의미
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
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/member/join",
                    "/css/**",
                    "/js/**",
                    "/image/**",
                    "/images/**"
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/vehicle/**", "/reservation/**", "/ai-chat/**").authenticated()

                .anyRequest().permitAll()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("userId")
                .passwordParameter("password")
                .successHandler(evLoginSuccessHandler)
                .failureHandler(evLoginFailureHandler)
                .permitAll()
            )
            
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
            	        response.sendRedirect("/login?error");
            	    })
            	)

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}