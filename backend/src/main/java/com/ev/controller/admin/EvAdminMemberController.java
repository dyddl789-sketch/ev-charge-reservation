package com.ev.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/member")
public class EvAdminMemberController {
	
    // 회원 목록 화면
    @GetMapping("/list")
    public String memberList() {
        return "admin/member/member_list";
    }
}