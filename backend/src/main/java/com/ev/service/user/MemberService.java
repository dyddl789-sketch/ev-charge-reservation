package com.ev.service.user;

import com.ev.dto.member.MemberDTO;

public interface MemberService {
	
	// 회원가입 처리
	void join(MemberDTO membeDTO);
	
	// 로그인 처리
	MemberDTO login(String userId, String password, String userType);
}
