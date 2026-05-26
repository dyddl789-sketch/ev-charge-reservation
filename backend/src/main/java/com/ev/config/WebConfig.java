package com.ev.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ev.interceptor.AdminCheckInterceptor;
import com.ev.interceptor.LoginCheckInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Web MVC 설정 클래스
 *
 * 역할:
 * - Interceptor 등록
 * - 로그인 체크 URL 설정
 * - 관리자 권한 체크 URL 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminCheckInterceptor adminCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        /*
         * 1. 로그인 체크 인터셉터
         *
         * 로그인하지 않은 사용자가
         * 예약, 차량, 관리자 페이지에 접근하지 못하게 막는다.
         */
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/reservation/**",
                        "/vehicle/**",
                        "/admin/**"
                )
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/member/join",
                        "/css/**",
                        "/js/**",
                        "/image/**",
                        "/images/**"
                );

        /*
         * 2. 관리자 권한 체크 인터셉터
         *
         * /admin/** 주소는 로그인 여부뿐 아니라
         * loginUserType이 ADMIN인지 확인한다.
         */
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns(
                        "/admin/**"
                )
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/image/**",
                        "/images/**"
                );
    }
}