package com.ev.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.admin.EvAdminMemberDAO;
import com.ev.dto.admin.EvAdminMemberChargingDTO;
import com.ev.dto.admin.EvAdminMemberDetailDTO;
import com.ev.dto.admin.EvAdminMemberPageDTO;
import com.ev.dto.admin.EvAdminMemberPaymentDTO;
import com.ev.dto.admin.EvAdminMemberReservationDTO;
import com.ev.dto.admin.EvAdminMemberRowDTO;
import com.ev.dto.admin.EvAdminMemberSearchDTO;
import com.ev.dto.admin.EvAdminMemberVehicleDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 회원 관리 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminMemberServiceImpl implements EvAdminMemberService {

    private final EvAdminMemberDAO evAdminMemberDAO;

    @Override
    public EvAdminMemberPageDTO getMemberPage(EvAdminMemberSearchDTO searchDTO) {

        log.info("@# EvAdminMemberServiceImpl.getMemberPage()");
        log.info("@# searchDTO => {}", searchDTO);

        Long totalCount = safeLong(evAdminMemberDAO.countTotalMembers());
        Long activeCount = safeLong(evAdminMemberDAO.countActiveMembers());
        Long inactiveCount = safeLong(evAdminMemberDAO.countInactiveMembers());

        Long searchCount = safeLong(evAdminMemberDAO.countMemberList(searchDTO));
        List<EvAdminMemberRowDTO> memberList =
                evAdminMemberDAO.findMemberList(searchDTO);

        EvAdminMemberPageDTO pageDTO = new EvAdminMemberPageDTO();

        pageDTO.setTotalCount(totalCount);
        pageDTO.setActiveCount(activeCount);
        pageDTO.setInactiveCount(inactiveCount);

        pageDTO.setSearchCount(searchCount);
        pageDTO.setMemberList(memberList);

        pageDTO.setPage(searchDTO.getPage());
        pageDTO.setSize(searchDTO.getSize());
        pageDTO.setTotalPage(calcTotalPage(searchCount, searchDTO.getSize()));

        log.info("@# totalCount => {}", totalCount);
        log.info("@# searchCount => {}", searchCount);

        return pageDTO;
    }

    @Override
    @Transactional
    public void withdrawMember(Long memberId) {

        log.info("@# EvAdminMemberServiceImpl.withdrawMember()");
        log.info("@# memberId => {}", memberId);

        evAdminMemberDAO.updateMemberStatus(memberId, "INACTIVE");
    }

    @Override
    @Transactional
    public void restoreMember(Long memberId) {

        log.info("@# EvAdminMemberServiceImpl.restoreMember()");
        log.info("@# memberId => {}", memberId);

        evAdminMemberDAO.updateMemberStatus(memberId, "ACTIVE");
    }

    // null 방지
    private Long safeLong(Long value) {
        if (value == null) {
            return 0L;
        }

        return value;
    }

    // 전체 페이지 계산
    private int calcTotalPage(Long totalCount, Integer size) {
        if (totalCount == null || totalCount == 0) {
            return 1;
        }

        return (int) Math.ceil(totalCount / (double) size);
    }
    
    @Override
    public EvAdminMemberDetailDTO getMemberDetail(Long memberId) {

        log.info("@# EvAdminMemberServiceImpl.getMemberDetail()");
        log.info("@# memberId => {}", memberId);

        EvAdminMemberDetailDTO detailDTO =
                evAdminMemberDAO.findMemberDetail(memberId);

        if (detailDTO == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        return detailDTO;
    }
    
    @Override
    public List<EvAdminMemberVehicleDTO> getMemberVehicleList(Long memberId) {
        log.info("@# EvAdminMemberServiceImpl.getMemberVehicleList()");
        return evAdminMemberDAO.findMemberVehicleList(memberId);
    }

    @Override
    public List<EvAdminMemberReservationDTO> getMemberReservationList(Long memberId) {
        log.info("@# EvAdminMemberServiceImpl.getMemberReservationList()");
        return evAdminMemberDAO.findMemberReservationList(memberId);
    }

    @Override
    public List<EvAdminMemberChargingDTO> getMemberChargingList(Long memberId) {
        log.info("@# EvAdminMemberServiceImpl.getMemberChargingList()");
        return evAdminMemberDAO.findMemberChargingList(memberId);
    }

    @Override
    public List<EvAdminMemberPaymentDTO> getMemberPaymentList(Long memberId) {
        log.info("@# EvAdminMemberServiceImpl.getMemberPaymentList()");
        return evAdminMemberDAO.findMemberPaymentList(memberId);
    }
}