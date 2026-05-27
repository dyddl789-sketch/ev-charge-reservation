package com.ev.service.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvMemberDAO;
import com.ev.dto.member.EvMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class EvMemberServiceImpl implements EvMemberService {
  
    private final EvMemberDAO memberDAO;

    
    @Override
    @Transactional // 회원가입 처리 중 오류가 발생하면 작업을 rollback
    public void join(EvMemberDTO memberDTO) {
        log.info("@# MemberServiceImpl.join()");
        log.info("@# memberDTO => {}", memberDTO);

       // 아이디 중복확인
        int userIdCount = memberDAO.countByUserId(memberDTO.getUserId());

        if (userIdCount > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 확인
        
        int emailCount = memberDAO.countByEmail(memberDTO.getEmail());

        if (emailCount > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 전화번호 중복 확인
        if (memberDTO.getPhone() != null && !memberDTO.getPhone().trim().equals("")) {

            int phoneCount = memberDAO.countByPhone(memberDTO.getPhone());

            if (phoneCount > 0) {
                throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
            }
        }

        // 기본값 설정
        memberDTO.setUserType("USER");
        memberDTO.setLoginType("LOCAL");
        memberDTO.setStatus("ACTIVE");

        //회원 정보 저장
        memberDAO.insertMember(memberDTO);

        log.info("@# 회원가입 완료 userId => {}", memberDTO.getUserId());
    }


    @Override
    public EvMemberDTO login(String userId, String password, String userType) {
        log.info("@# MemberServiceImpl.login()");
        log.info("@# userId => {}", userId);
        log.info("@# selected userType => {}", userType);

        // 1. 아이디로 회원 조회
        EvMemberDTO loginMember = memberDAO.findByUserId(userId);

        // 2. 회원이 없거나 ACTIVE 상태가 아니면 로그인 실패
        if (loginMember == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 비밀번호 확인
        // 현재는 평문 비교. 나중에 BCrypt 적용하면 matches()로 변경
        if (!loginMember.getPassword().equals(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 4. 로그인 화면에서 선택한 탭과 DB 권한 비교
        // userType: login.jsp hidden input 값 USER / ADMIN
        // loginMember.getUserType(): DB app_member.user_type 값 USER / ADMIN
        if (!userType.equals(loginMember.getUserType())) {

            if ("ADMIN".equals(userType)) {
                throw new IllegalArgumentException("관리자 계정만 관리자 로그인이 가능합니다.");
            }

            if ("USER".equals(userType)) {
                throw new IllegalArgumentException("일반회원 계정만 일반회원 로그인이 가능합니다.");
            }

            throw new IllegalArgumentException("로그인 유형이 올바르지 않습니다.");
        }

        return loginMember;
    }


	
}










