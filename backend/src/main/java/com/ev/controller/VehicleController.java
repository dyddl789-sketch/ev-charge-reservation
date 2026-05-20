package com.ev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vehicle")
public class VehicleController {
	
	// 내 차량 목록 화면
    @GetMapping("/list")
    public String vehicleList() {
        return "vehicle/vehicle_list";
    }

    // 차량 등록 화면
    @GetMapping("/register")
    public String vehicleRegister() {
        return "vehicle/vehicle_register";
    }
}
