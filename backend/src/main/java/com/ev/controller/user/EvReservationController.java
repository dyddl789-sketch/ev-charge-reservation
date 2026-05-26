package com.ev.controller.user;

import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reservation")
public class EvReservationController {
	
	
	// 예약 메인 화면
    @GetMapping("")
    public String reservation() {
        return "user/reservation/reservation";
    }
    
    // 예약하기 화면
    @GetMapping("/form")
    public String reservationForm() {
        return "user/reservation/reservation_form";
    }

    // 예약 확정 처리
    // 지금은 화면 테스트용으로 DB 저장 없이 완료 페이지로 이동
    // 나중에 여기서 reservation 테이블 insert 처리하면 됨
    @PostMapping("/save")
    public String saveReservation(@RequestParam HashMap<String, String> param, Model model) {
        System.out.println("@# reservation param => " + param);

        model.addAttribute("reservation", param);

        return "user/reservation/reservation_complete";
    }

    // 예약 완료 화면 직접 확인용
    @GetMapping("/complete")
    public String reservationComplete() {
        return "user/reservation/reservation_complete";
    }

    // 내 예약 화면
    @GetMapping("/my")
    public String myReservation() {
        return "user/reservation/my_reservation";
    }
}