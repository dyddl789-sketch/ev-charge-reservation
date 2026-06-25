package com.ev.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.admin.EvAdminReservationDAO;
import com.ev.dto.admin.reservation.EvAdminReservationDetailDTO;
import com.ev.dto.admin.reservation.EvAdminReservationIssueDTO;
import com.ev.dto.admin.reservation.EvAdminReservationListDTO;
import com.ev.dto.admin.reservation.EvAdminReservationPageDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSearchDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSummaryDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminReservationServiceImpl implements EvAdminReservationService {

    private final EvAdminReservationDAO evAdminReservationDAO;

    @Override
    public EvAdminReservationPageDTO getReservationPage(EvAdminReservationSearchDTO searchDTO) {

        log.info("@# EvAdminReservationServiceImpl.getReservationPage()");

        if (searchDTO.getPage() == null || searchDTO.getPage() < 1) {
            searchDTO.setPage(1);
        }

        if (searchDTO.getSize() == null || searchDTO.getSize() < 1) {
            searchDTO.setSize(10);
        }

        EvAdminReservationSummaryDTO summary =
                evAdminReservationDAO.findReservationSummary(searchDTO);

        EvAdminReservationIssueDTO issue =
                evAdminReservationDAO.findTodayReservationIssue();

        Long totalCount =
                evAdminReservationDAO.countReservationList(searchDTO);

        List<EvAdminReservationListDTO> reservationList =
                evAdminReservationDAO.findReservationList(searchDTO);

        int totalPage = (int) Math.ceil((double) totalCount / searchDTO.getSize());

        EvAdminReservationPageDTO pageDTO = new EvAdminReservationPageDTO();

        pageDTO.setSummary(summary);
        pageDTO.setIssue(issue);
        pageDTO.setReservationList(reservationList);
        pageDTO.setTotalCount(totalCount);
        pageDTO.setPage(searchDTO.getPage());
        pageDTO.setSize(searchDTO.getSize());
        pageDTO.setTotalPage(totalPage == 0 ? 1 : totalPage);

        return pageDTO;
    }


    @Override
    public EvAdminReservationDetailDTO getReservationDetail(Long reservationId) {

        log.info("@# EvAdminReservationServiceImpl.getReservationDetail()");
        log.info("@# reservationId => {}", reservationId);

        EvAdminReservationDetailDTO detailDTO = evAdminReservationDAO.findReservationDetail(reservationId);

        if (detailDTO == null) {
            throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
        }

        return detailDTO;
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {

        log.info("@# EvAdminReservationServiceImpl.cancelReservation()");
        log.info("@# reservationId => {}", reservationId);

        evAdminReservationDAO.cancelReservation(reservationId);
    }

    @Override
    @Transactional
    public void noShowReservation(Long reservationId) {

        log.info("@# EvAdminReservationServiceImpl.noShowReservation()");
        log.info("@# reservationId => {}", reservationId);

        evAdminReservationDAO.noShowReservation(reservationId);
    }

    @Override
    @Transactional
    public void startCharging(Long reservationId) {

        log.info("@# EvAdminReservationServiceImpl.startCharging()");
        log.info("@# reservationId => {}", reservationId);

        evAdminReservationDAO.startCharging(reservationId);
    }
}