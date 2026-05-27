package com.ev.dao.user;

import org.apache.ibatis.annotations.Mapper;

import com.ev.dto.member.EvMemberDTO;

/*
 * 회원가입에서 필요한 기능:
 * 1. 아이디 중복 확인
 * 2. 이메일 중복 확인
 * 3. 전화번호 중복 확인
 * 4. 회원 정보 저장
*/
@Mapper
public interface EvMemberDAO {
    /*
     * 아이디 중복 확인
     */
    int countByUserId(String userId);

    /*
     * 이메일 중복 확인
     */
    int countByEmail(String email);

    /*
     * 전화번호 중복 확인
     */
    int countByPhone(String phone);

    /*
     * 회원가입 정보 저장
     */
    void insertMember(EvMemberDTO memberDTO);
    
    /*
     * 로그인용 회원 조회
     *
     * user_id로 회원 1명을 조회한다.
     */
    EvMemberDTO findByUserId(String userId);

    //소셜로그인 정보 저장
    void insertSocialMember(EvMemberDTO evMemberDTO);
}