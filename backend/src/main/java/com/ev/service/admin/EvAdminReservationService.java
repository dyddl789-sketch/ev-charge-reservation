package com.ev.service.admin;

import com.ev.dto.admin.reservation.EvAdminReservationDetailDTO;
import com.ev.dto.admin.reservation.EvAdminReservationPageDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSearchDTO;

public interface EvAdminReservationService {

    EvAdminReservationPageDTO getReservationPage(EvAdminReservationSearchDTO searchDTO);

    EvAdminReservationDetailDTO getReservationDetail(Long reservationId);

    void cancelReservation(Long reservationId);

    void noShowReservation(Long reservationId);

    void startCharging(Long reservationId);
}