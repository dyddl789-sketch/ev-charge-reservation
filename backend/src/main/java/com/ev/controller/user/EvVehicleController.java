package com.ev.controller.user;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvVehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/vehicle")
@Slf4j
@RequiredArgsConstructor
public class EvVehicleController {

    private final EvVehicleService evVehicleService;

    // 내 차량 목록 화면
    @GetMapping("/list")
    public String vehicleList(
            @AuthenticationPrincipal EvUserDetails userDetails,
            Model model) {

        Long memberId = userDetails.getMemberId();

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

        model.addAttribute("vehicleModelList", vehicleModelList);

        return "user/vehicle/vehicle_register";
    }

    // 차량 등록 처리
    @PostMapping("/register")
    public String vehicleRegisterProcess(
            @AuthenticationPrincipal EvUserDetails userDetails,
            EvVehicleDTO vehicleDTO,
            RedirectAttributes rttr) {

        Long memberId = userDetails.getMemberId();

        vehicleDTO.setMemberId(memberId);

        log.info("vehicleDTO={}", vehicleDTO);

        evVehicleService.registerVehicle(vehicleDTO);

        rttr.addFlashAttribute("msg", "차량 등록이 완료되었습니다.");

        return "redirect:/vehicle/list";
    }

    // 기본차량 설정 ajax방식
    @PostMapping("/default")
    @ResponseBody
    public String setDefaultVehicle(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestParam("vehicleId") Long vehicleId) {

        Long memberId = userDetails.getMemberId();

        log.info("setDefaultVehicle memberId={}, vehicleId={}",
                memberId,
                vehicleId);

        evVehicleService.setDefaultVehicle(memberId, vehicleId);

        return "success";
    }

    // 차량 삭제 (논리삭제 적용)
    @PostMapping("/delete")
    @ResponseBody
    public String deleteVehicle(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestParam("vehicleId") Long vehicleId) {

        Long memberId = userDetails.getMemberId();

        log.info("deleteVehicle memberId={}, vehicleId={}",
                memberId,
                vehicleId);

        evVehicleService.deleteVehicle(memberId, vehicleId);

        return "success";
    }
}