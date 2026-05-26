package com.ev.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 체크 인터셉터
 *
 * 역할:
 * - 로그인하지 않은 사용자가 예약, 차량, 관리자 페이지 등에 접근하는 것을 막는다.
 *
 * 동작 흐름:
 * 1. 요청이 Controller에 도착하기 전에 preHandle() 실행
 * 2. session에서 loginMemberId 확인
 * 3. 값이 없으면 로그인하지 않은 상태
 * 4. /login으로 이동
 * 5. 값이 있으면 정상 통과
 */
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    /**
     * Controller 실행 전에 호출되는 메서드
     *
     * return true  → Controller로 요청 진행
     * return false → Controller로 가지 않고 요청 중단
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        log.info("@# LoginCheckInterceptor.preHandle()");

        /*
         * 현재 요청 URI 확인
         *
         * 예:
         * /reservation/my
         * /vehicle/list
         * /admin/dashboard
         */
        String requestURI = request.getRequestURI();
        log.info("@# requestURI => {}", requestURI);

        /*
         * 세션 가져오기
         *
         * false:
         * - 기존 세션이 있으면 가져오고
         * - 없으면 새로 만들지 않음
         */
        HttpSession session = request.getSession(false);

        /*
         * 로그인 여부 확인
         *
         * loginMemberId는 로그인 성공 시 EvAuthController에서 저장한 값
         */
        if (session == null || session.getAttribute("loginMemberId") == null) {

            log.info("@# 비로그인 사용자 접근 차단");

            /*
             * 로그인하지 않은 사용자는 로그인 화면으로 이동
             */
            response.sendRedirect("/login?authMsg=loginRequired");

            return false;
        }

        log.info("@# 로그인 사용자 접근 허용");

        return true;
    }
}