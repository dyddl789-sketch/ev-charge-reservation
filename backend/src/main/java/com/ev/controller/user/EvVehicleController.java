package com.ev.controller.user;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.dto.vehicle.EvVehicleModelDTO;
import com.ev.service.user.EvVehicleService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/vehicle")
@Slf4j
public class EvVehicleController {
	
	@Autowired
	private EvVehicleService evVehicleService;
	
	// 내 차량 목록 화면
	@GetMapping("/list")
	public String vehicleList(Model model) {

	    Long memberId = 1L; // 로그인 전 임시 회원
	    
	    log.info("vehicleList memberId={}", memberId);
	    
	    List<EvVehicleDTO> vehicleList =
	    		evVehicleService.getVehicleList(memberId);
	    
	    log.info("vehicleList size={}", vehicleList.size());

	    model.addAttribute("vehicleList", vehicleList);

	    return "user/vehicle/vehicle_list";
	}

    // 차량 등록 화면 진입
    @GetMapping("/register")
    public String vehicleRegister(Model model) {

        List<EvVehicleModelDTO> vehicleModelList =
        		evVehicleService.getVehicleModelList();

        log.info("vehicleModelList={}", vehicleModelList);

        model.addAttribute(
                "vehicleModelList",
                vehicleModelList
        );

        return "user/vehicle/vehicle_register";
    }
    
    // 차량 등록 처리
    @PostMapping("/register")
    public String vehicleRegisterProcess(
            EvVehicleDTO vehicleDTO,
            RedirectAttributes rttr) {

        // 로그인 구현 전 임시 회원 ID
        vehicleDTO.setMemberId(1L);

        log.info("vehicleDTO={}", vehicleDTO);

        evVehicleService.registerVehicle(vehicleDTO);

        rttr.addFlashAttribute("msg", "차량 등록이 완료되었습니다.");

        return "redirect:/vehicle/list";
    }
    
    //기본차량 설정 ajax방식
    @PostMapping("/default")
    @ResponseBody
    public String setDefaultVehicle(@RequestParam("vehicleId") Long vehicleId,
                                    HttpSession session) {

        //Long memberId = (Long) session.getAttribute("memberId");
    	Long memberId = 1L; // 로그인 전 임시 회원
        
        log.info("setDefaultVehicle memberId={}, vehicleId={}",
                memberId,
                vehicleId);

        evVehicleService.setDefaultVehicle(memberId, vehicleId);

        return "success";
    }
    
    //차량 삭제 (논리삭제 적용)
    @PostMapping("/delete")
    @ResponseBody
    public String deleteVehicle(@RequestParam("vehicleId") Long vehicleId,
                                HttpSession session) {

        //Long memberId = (Long) session.getAttribute("memberId");
    	Long memberId = 1L; // 로그인 전 임시 회원
        
        log.info("deleteVehicle memberId={}, vehicleId={}",
                memberId,
                vehicleId);

        evVehicleService.deleteVehicle(memberId, vehicleId);

        return "success";
    }
}
