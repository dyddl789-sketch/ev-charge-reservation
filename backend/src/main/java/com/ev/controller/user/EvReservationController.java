package com.ev.controller.user;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;


import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 사용자 예약 Controller
 */
@Slf4j
@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class EvReservationController {

    private final EvReservationService evReservationService;

    /*
     * 예약 폼 화면
     *
     * 요청 URL:
     * GET /reservation/form?chargerId=1
     */
    @GetMapping("/form")
    public String reservationForm(@RequestParam("chargerId") Long chargerId,
                                  @AuthenticationPrincipal EvUserDetails userDetails,
                                  Model model,
                                  RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationForm()");
        log.info("@# chargerId => {}", chargerId);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        try {
            /*
             * 처음 선택해서 들어온 충전기
             */
            EvReservationChargerDTO charger =
                    evReservationService.getReservationCharger(chargerId);

            if (!"사용가능".equals(charger.getChargerStatus())) {
                rttr.addFlashAttribute("errorMsg", "현재 예약 가능한 충전기가 아닙니다.");
                return "redirect:/station/map";
            }

            /*
             * 처음 진입한 충전기 Redis 임시 점유
             */
            boolean holdSuccess =
                    evReservationService.holdChargerForReservation(chargerId, memberId);

            if (!holdSuccess) {
                rttr.addFlashAttribute(
                        "errorMsg",
                        "다른 사용자가 해당 충전기 예약 정보를 입력 중입니다. 잠시 후 다시 시도해주세요."
                );

                return "redirect:/station/map";
            }

            /*
             * 같은 충전소의 전체 충전기 목록
             */
            List<EvReservationChargerDTO> chargerList =
                    evReservationService.getReservationChargerList(charger.getStationId(), memberId);

            List<EvVehicleDTO> vehicleList =
                    evReservationService.getVehicleList(memberId);

            model.addAttribute("charger", charger);
            model.addAttribute("chargerList", chargerList);
            model.addAttribute("vehicleList", vehicleList);

            return "user/reservation/reservation_form";

        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/station/map";
        }
    }
    
    /*
     * 예약 입력 중 충전기 임시 점유 해제
     *
     * 요청 URL:
     * POST /reservation/lock/release
     *
     * 사용자가 예약 폼 화면을 벗어날 때 호출된다.
     * 호출이 실패해도 Redis TTL이 지나면 자동 해제된다.
     */
    @PostMapping("/lock/release")
    @ResponseBody
    public String releaseReservationLock(@RequestParam("chargerId") Long chargerId,
                                         @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationController.releaseReservationLock()");
        log.info("@# chargerId => {}", chargerId);

        if (userDetails == null) {
            return "LOGIN_REQUIRED";
        }

        Long memberId = userDetails.getMemberId();

        evReservationService.releaseChargerReservationHold(chargerId, memberId);

        return "OK";
    }
    
    
    /*
     * 예약 등록 처리
     *
     * 요청 URL:
     * POST /reservation/register
     */
    @PostMapping("/register")
    public String reservationRegister(EvReservationDTO reservationDTO,
                                      @AuthenticationPrincipal EvUserDetails userDetails,
                                      RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationRegister()");
        log.info("@# reservationDTO => {}", reservationDTO);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        try {
            /*
             * memberId는 화면에서 받지 않고
             * Spring Security 로그인 사용자 정보에서 넣는다.
             */
            reservationDTO.setMemberId(memberId);

            Long reservationId =
                    evReservationService.createReservation(reservationDTO);

            rttr.addFlashAttribute("msg", "예약이 완료되었습니다.");

            return "redirect:/reservation/complete?reservationId=" + reservationId;

        } catch (IllegalArgumentException e) {
            log.info("@# reservation error => {}", e.getMessage());

            rttr.addFlashAttribute("errorMsg", e.getMessage());

            return "redirect:/reservation/form?chargerId=" + reservationDTO.getChargerId();
        }
    }

    /*
     * 예약 완료 화면
     *
     * 요청 URL:
     * GET /reservation/complete?reservationId=1
     */
    @GetMapping("/complete")
    public String reservationComplete(@RequestParam("reservationId") Long reservationId,
                                      @AuthenticationPrincipal EvUserDetails userDetails,
                                      Model model,
                                      RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationComplete()");
        log.info("@# reservationId => {}", reservationId);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        EvReservationDTO reservation =
                evReservationService.getReservationComplete(reservationId, memberId);

        if (reservation == null) {
            rttr.addFlashAttribute("errorMsg", "예약 정보를 찾을 수 없습니다.");
            return "redirect:/station/map";
        }

        model.addAttribute("reservation", reservation);

        return "user/reservation/reservation_complete";
    }

    /*
     * 내 예약 목록 화면
     *
     * 요청 URL:
     * GET /reservation/my
     */
    @GetMapping("/my")
    public String myReservationList(@AuthenticationPrincipal EvUserDetails userDetails,
                                    Model model,
                                    RedirectAttributes rttr) {
        log.info("@# EvReservationController.myReservationList()");

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        log.info("@# memberId => {}", memberId);

        List<EvReservationDTO> reservationList =
                evReservationService.getMyReservationList(memberId);

        model.addAttribute("reservationList", reservationList);

        return "user/reservation/my_reservation";
    }

    /*
     * 예약 취소 처리
     *
     * 요청 URL:
     * POST /reservation/cancel
     */
    @PostMapping("/cancel")
    public String cancelReservation(@RequestParam("reservationId") Long reservationId,
                                    @AuthenticationPrincipal EvUserDetails userDetails,
                                    RedirectAttributes rttr) {
        log.info("@# EvReservationController.cancelReservation()");
        log.info("@# reservationId => {}", reservationId);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        try {
            evReservationService.cancelReservation(reservationId, memberId);

            rttr.addFlashAttribute("msg", "예약이 취소되었습니다.");

        } catch (IllegalArgumentException e) {
            log.info("@# cancel reservation error => {}", e.getMessage());

            rttr.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/reservation/my";
    }

    /*
     * 예약 현장 인증코드 발급
     *
     *
     * 처리 내용:
     * 1. 로그인 사용자 확인
     * 2. reservationId 받기
     * 3. Service에서 본인 예약 + 예약 상태 + 예약 시간 확인
     * 4. Redis에 충전기별 인증코드 발급
     * 5. 내 예약 화면으로 돌아가면서 모달에 인증코드 표시
     */
    @PostMapping("/auth-code/issue")
    public String issueAuthCode(@RequestParam("reservationId") Long reservationId,
                                @AuthenticationPrincipal EvUserDetails userDetails,
                                RedirectAttributes rttr) {
        log.info("@# EvReservationController.issueAuthCode()");
        log.info("@# reservationId => {}", reservationId);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        try {
            String authCode =
                    evReservationService.issueAuthCode(reservationId, memberId);

            rttr.addFlashAttribute("issuedReservationId", reservationId);
            rttr.addFlashAttribute("issuedAuthCode", authCode);
            rttr.addFlashAttribute("msg", "인증코드가 발급되었습니다. 5분 안에 입력해주세요.");

        } catch (IllegalArgumentException e) {
            log.info("@# issue auth code error => {}", e.getMessage());

            rttr.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/reservation/my";
    }

    /*
     * 예약 현장 인증 처리
     *
     * 요청 URL:
     * POST /reservation/verify
     *
     * 처리 내용:
     * 1. 로그인 사용자 확인
     * 2. reservationId, authCode 받기
     * 3. 로그인한 memberId를 Service로 전달
     * 4. Service에서 본인 예약 + 예약 시간 + Redis 인증코드 검증
     * 5. 성공 시 내 예약 화면으로 이동
     */
    @PostMapping("/verify")
    public String verifyReservation(@RequestParam("reservationId") Long reservationId,
                                    @RequestParam("authCode") String authCode,
                                    @AuthenticationPrincipal EvUserDetails userDetails,
                                    RedirectAttributes rttr) {
        log.info("@# EvReservationController.verifyReservation()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# authCode => {}", authCode);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (authCode == null || authCode.trim().isEmpty()) {
            rttr.addFlashAttribute("errorMsg", "인증코드를 입력해주세요.");
            return "redirect:/reservation/my";
        }

        Long memberId = userDetails.getMemberId();

        try {
            evReservationService.verifyReservation(
                    reservationId,
                    memberId,
                    authCode.trim()
            );

            rttr.addFlashAttribute("msg", "예약 인증이 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            log.info("@# verify reservation error => {}", e.getMessage());

            rttr.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/reservation/my";
    }

    /*
     * 충전 내역 페이지
     *
     * 요청 URL:
     * GET /reservation/history
     *
     * 처리 내용:
     * 1. 로그인 사용자 확인
     * 2. 로그인 사용자의 충전 완료 내역 조회
     * 3. charging_history.jsp로 이동
     */
    @GetMapping("/history")
    public String chargingHistory(@AuthenticationPrincipal EvUserDetails userDetails,
                                  Model model,
                                  RedirectAttributes rttr) {
        log.info("@# EvReservationController.chargingHistory()");

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();
        log.info("@# memberId => {}", memberId);

        List<EvReservationDTO> chargingHistoryList =
                evReservationService.getChargingHistoryList(memberId);

        model.addAttribute("chargingHistoryList", chargingHistoryList);

        return "user/reservation/charging_history";
    }

    /*
     * 충전 영수증 이메일 발송
     */
    @PostMapping("/receipt/email")
    public String sendReceiptEmail(@RequestParam("reservationId") Long reservationId,
                                   @AuthenticationPrincipal EvUserDetails userDetails,
                                   RedirectAttributes rttr) {
        log.info("@# EvReservationController.sendReceiptEmail()");
        log.info("@# reservationId => {}", reservationId);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        try {
            evReservationService.sendReceiptEmail(reservationId, memberId);

            rttr.addFlashAttribute("msg", "고객님의 이메일로 영수증이 발급되었습니다.");

        } catch (IllegalArgumentException e) {
            log.info("@# receipt email error => {}", e.getMessage());

            rttr.addFlashAttribute("errorMsg", e.getMessage());

        } catch (Exception e) {
            log.error("@# receipt email send fail", e);

            rttr.addFlashAttribute("errorMsg", "영수증 이메일 발송 중 오류가 발생했습니다.");
        }

        return "redirect:/reservation/history";
    }
    
    /*
     * 예약 폼에서 선택 충전기 변경
     *
     * 요청 URL:
     * POST /reservation/lock/change
     */
    @PostMapping("/lock/change")
    @ResponseBody
    public Map<String, Object> changeReservationLock(
            @RequestParam("oldChargerId") Long oldChargerId,
            @RequestParam("newChargerId") Long newChargerId,
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvReservationController.changeReservationLock()");
        log.info("@# oldChargerId => {}", oldChargerId);
        log.info("@# newChargerId => {}", newChargerId);

        Map<String, Object> result = new HashMap<>();

        if (userDetails == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        Long memberId = userDetails.getMemberId();
        log.info("@# memberId => {}", memberId);

        try {
            boolean success = evReservationService.changeReservationHold(
                    oldChargerId,
                    newChargerId,
                    memberId
            );

            result.put("success", success);

            if (!success) {
                result.put("message", "다른 사용자가 선택 중인 충전기입니다.");
            }

        } catch (Exception e) {
            log.info("@# change reservation lock error => {}", e.getMessage());

            result.put("success", false);
            result.put("message", "충전기 선택 변경 중 오류가 발생했습니다.");
        }

        return result;
    }
    
    /*
     * 충전소 기준 예약 폼 진입
     *
     * 요청 URL:
     * GET /reservation/form/station?stationId=1
     *
     * 지도 화면의 예약하기 버튼에서 호출한다.
     *
     * 처리:
     * 1. 해당 충전소의 충전기 목록 조회
     * 2. 사용가능 충전기 중 Redis Lock 가능한 충전기 찾기
     * 3. 찾으면 /reservation/form?chargerId=선택된충전기 로 이동
     * 4. 없으면 충전소 탐색 화면으로 돌려보냄
     */
    @GetMapping("/form/station")
    public String reservationFormByStation(@RequestParam("stationId") Long stationId,
                                           @AuthenticationPrincipal EvUserDetails userDetails,
                                           RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationFormByStation()");
        log.info("@# stationId => {}", stationId);

        if (userDetails == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Long memberId = userDetails.getMemberId();

        List<EvReservationChargerDTO> chargerList =
                evReservationService.getReservationChargerList(stationId, memberId);

        if (chargerList == null || chargerList.isEmpty()) {
            rttr.addFlashAttribute("errorMsg", "등록된 충전기가 없습니다.");
            return "redirect:/station/map?stationId=" + stationId;
        }

        /*
         * 사용가능 + Redis Lock 가능 충전기를 찾는다.
         *
         * 예:
         * - 9번이 이미 다른 사용자에게 Lock 되어 있으면 실패
         * - 다음 사용가능 충전기를 계속 시도
         */
        for (EvReservationChargerDTO charger : chargerList) {
            if (!"사용가능".equals(charger.getChargerStatus())) {
                continue;
            }

            boolean holdSuccess =
                    evReservationService.holdChargerForReservation(
                            charger.getChargerId(),
                            memberId
                    );

            if (holdSuccess) {
                log.info("@# selected chargerId => {}", charger.getChargerId());

                return "redirect:/reservation/form?chargerId=" + charger.getChargerId();
            }
        }

        rttr.addFlashAttribute(
                "errorMsg",
                "현재 예약 가능한 충전기가 없거나 다른 사용자가 예약 정보를 입력 중입니다."
        );

        return "redirect:/station/map?stationId=" + stationId;
    }
}