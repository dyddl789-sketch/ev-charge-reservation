package com.ev.controller.user;

import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicle")
public class EvVehicleController {
	
	// 내 차량 목록 화면
    @GetMapping("/list")
    public String vehicleList() {
        return "user/vehicle/vehicle_list";
    }

    // 차량 등록 화면
    @GetMapping("/register")
    public String vehicleRegister() {
        return "user/vehicle/vehicle_register";
    }
    
    // 차량 등록 처리
    @PostMapping("/registerProcess")
    public String vehicleRegisterProcess(
            @RequestParam HashMap<String, String> param,
            RedirectAttributes rttr) {

        System.out.println("@# vehicle register param => " + param);

        // 나중에 여기에서 service.registerVehicle(param) 형태로 DB insert 처리
        // 현재는 화면 흐름 테스트용

        rttr.addFlashAttribute("msg", "차량 등록이 완료되었습니다.");

        return "redirect:/vehicle/list";
    }
}
