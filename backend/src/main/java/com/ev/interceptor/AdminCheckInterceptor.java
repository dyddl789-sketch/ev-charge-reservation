package com.ev.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 권한 체크 인터셉터
 *
 * 역할:
 * - /admin/** 주소에 접근할 때
 * - 로그인한 사용자가 ADMIN 권한인지 확인한다.
 *
 * 전제:
 * - 로그인 성공 시 EvAuthController에서 세션에 loginUserType을 저장해야 한다.
 *
 * 예:
 * session.setAttribute("loginUserType", loginMember.getUserType());
 */
@Slf4j
@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    /**
     * Controller 실행 전에 호출됨
     *
     * return true  : Controller로 요청 진행
     * return false : 요청 중단
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        log.info("@# AdminCheckInterceptor.preHandle()");

        String requestURI = request.getRequestURI();
        log.info("@# admin requestURI => {}", requestURI);

        /*
         * 기존 세션 가져오기
         *
         * false:
         * - 세션이 있으면 가져오고
         * - 없으면 새로 만들지 않음
         */
        HttpSession session = request.getSession(false);

        /*
         * 세션이 없으면 로그인하지 않은 상태
         *
         * 원래 LoginCheckInterceptor에서 먼저 막지만,
         * 안전하게 여기서도 한 번 더 확인한다.
         */
        if (session == null || session.getAttribute("loginMemberId") == null) {
            log.info("@# 관리자 페이지 접근 차단 - 비로그인 상태");

            response.sendRedirect("/login");
            return false;
        }

        /*
         * 로그인한 사용자의 권한 확인
         *
         * EvAuthController에서 저장한 세션 이름:
         * loginUserType
         */
        String loginUserType = (String) session.getAttribute("loginUserType");

        log.info("@# loginUserType => {}", loginUserType);

        /*
         * ADMIN이 아니면 관리자 페이지 접근 차단
         */
        if (!"ADMIN".equals(loginUserType)) {
            log.info("@# 관리자 페이지 접근 차단 - ADMIN 권한 아님");

            /*
             * 일반 사용자는 메인으로 돌려보낸다.
             * 필요하면 /login으로 보내도 된다.
             */
            response.sendRedirect("/main?authMsg=adminOnly");
            return false;
        }

        log.info("@# 관리자 페이지 접근 허용");

        return true;
    }
}