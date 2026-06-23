package com.ev.controller.user;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.dto.vehicle.EvVehicleModelDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvAiChatService;
import com.ev.service.user.EvVehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/vehicle/api")
@RequiredArgsConstructor
public class EvVehicleApiController {

    private final EvVehicleService evVehicleService;
    private final EvAiChatService evAiChatService;

    @GetMapping("/models")
    public List<EvVehicleModelDTO> getVehicleModelList() {
        log.info("@# EvVehicleApiController.getVehicleModelList()");
        return evVehicleService.getVehicleModelList();
    }

    @GetMapping("/list")
    public ResponseEntity<?> getVehicleList(@AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvVehicleApiController.getVehicleList()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long memberId = userDetails.getMemberId();
        return ResponseEntity.ok(evVehicleService.getVehicleList(memberId));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVehicle(@AuthenticationPrincipal EvUserDetails userDetails,
                                             EvVehicleDTO formDTO,
                                             @RequestBody(required = false) EvVehicleDTO jsonDTO) {
        log.info("@# EvVehicleApiController.registerVehicle()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        EvVehicleDTO vehicleDTO = jsonDTO != null ? jsonDTO : formDTO;
        vehicleDTO.setMemberId(userDetails.getMemberId());

        try {
            evVehicleService.registerVehicle(vehicleDTO);

            // 대표차량/차량 정보가 바뀌면 AI가 이전 차량 기준으로 답하지 않도록 Redis 대화 캐시를 정리한다.
            evAiChatService.clearChatCache(userDetails.getMemberId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "차량 등록이 완료되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/default")
    public ResponseEntity<?> setDefaultVehicle(@AuthenticationPrincipal EvUserDetails userDetails,
                                               @RequestParam("vehicleId") Long vehicleId) {
        log.info("@# EvVehicleApiController.setDefaultVehicle()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        evVehicleService.setDefaultVehicle(userDetails.getMemberId(), vehicleId);
        evAiChatService.clearChatCache(userDetails.getMemberId());

        return ResponseEntity.ok(Map.of("success", true, "message", "기본 차량이 변경되었습니다."));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteVehicle(@AuthenticationPrincipal EvUserDetails userDetails,
                                           @RequestParam("vehicleId") Long vehicleId) {
        log.info("@# EvVehicleApiController.deleteVehicle()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        evVehicleService.deleteVehicle(userDetails.getMemberId(), vehicleId);
        evAiChatService.clearChatCache(userDetails.getMemberId());

        return ResponseEntity.ok(Map.of("success", true, "message", "차량이 삭제되었습니다."));
    }
}
