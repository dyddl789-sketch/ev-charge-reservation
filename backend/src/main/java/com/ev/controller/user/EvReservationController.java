package com.ev.controller.user;

import java.util.List;

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
import com.ev.service.user.EvReservationService;

import jakarta.servlet.http.HttpSession;
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
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationForm()");
        log.info("@# chargerId => {}", chargerId);

        Long memberId = (Long) session.getAttribute("loginMemberId");

        if (memberId == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            EvReservationChargerDTO charger = evReservationService.getReservationCharger(chargerId);
            List<EvVehicleDTO> vehicleList = evReservationService.getVehicleList(memberId);

            model.addAttribute("charger", charger);
            model.addAttribute("vehicleList", vehicleList);

            return "user/reservation/reservation_form";

        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/station/list";
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
                                      HttpSession session,
                                      RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationRegister()");
        log.info("@# reservationDTO => {}", reservationDTO);

        Long memberId = (Long) session.getAttribute("loginMemberId");

        if (memberId == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            /*
             * memberId는 화면에서 받지 않고 세션에서 넣는다.
             */
            reservationDTO.setMemberId(memberId);

            Long reservationId = evReservationService.createReservation(reservationDTO);

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
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes rttr) {
        log.info("@# EvReservationController.reservationComplete()");
        log.info("@# reservationId => {}", reservationId);

        Long memberId = (Long) session.getAttribute("loginMemberId");

        if (memberId == null) {
            rttr.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        EvReservationDTO reservation = evReservationService.getReservationComplete(reservationId, memberId);

        if (reservation == null) {
            rttr.addFlashAttribute("errorMsg", "예약 정보를 찾을 수 없습니다.");
            return "redirect:/station/list";
        }

        model.addAttribute("reservation", reservation);

        return "user/reservation/reservation_complete";
    }
}