package com.ev.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dao.MemberDAO;
import com.ev.dto.MemberDTO;

@RestController
public class DBTestController {

    private final MemberDAO memberDAO;

    public DBTestController(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    @GetMapping("/db/test")
    public MemberDTO dbTest() {

        return memberDAO.findMemberById(1);
    }
}