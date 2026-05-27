package com.ev.service.user;

import com.ev.dto.member.EvMemberDTO;

public interface EvMemberService {
	
	// 회원가입 처리
	void join(EvMemberDTO membeDTO);
	
	// 로그인 처리
	EvMemberDTO login(String userId, String password, String userType);
}
