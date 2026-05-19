package com.ev.dao;

import org.apache.ibatis.annotations.Mapper;
import com.ev.dto.MemberDTO;

@Mapper
public interface MemberDAO {

    MemberDTO findMemberById(int memberId);
}