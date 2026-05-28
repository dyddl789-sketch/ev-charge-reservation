package com.ev.controller.user;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            EvReservationChargerDTO charger =
                    evReservationService.getReservationCharger(chargerId);

            List<EvVehicleDTO> vehicleList =
                    evReservationService.getVehicleList(memberId);

            model.addAttribute("charger", charger);
            model.addAttribute("vehicleList", vehicleList);

            return "user/reservation/reservation_form";

        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/station/map";
        }
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
     * 요청 URL:
     * POST /reservation/auth-code/issue
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
}
