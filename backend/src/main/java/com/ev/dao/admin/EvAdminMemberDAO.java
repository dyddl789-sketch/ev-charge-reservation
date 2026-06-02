package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.EvAdminMemberRowDTO;
import com.ev.dto.admin.EvAdminMemberSearchDTO;
import com.ev.dto.admin.EvAdminMemberVehicleDTO;
import com.ev.dto.admin.EvAdminMemberChargingDTO;
import com.ev.dto.admin.EvAdminMemberDetailDTO;
import com.ev.dto.admin.EvAdminMemberPaymentDTO;
import com.ev.dto.admin.EvAdminMemberReservationDTO;

@Mapper
public interface EvAdminMemberDAO {

    // 전체 회원 수
    Long countTotalMembers();

    // 활성 회원 수
    Long countActiveMembers();

    // 탈퇴 회원 수
    Long countInactiveMembers();

    // 검색 결과 수
    Long countMemberList(EvAdminMemberSearchDTO searchDTO);

    // 회원 목록 조회
    List<EvAdminMemberRowDTO> findMemberList(EvAdminMemberSearchDTO searchDTO);

    // 회원 상태 변경
    void updateMemberStatus(
            @Param("memberId") Long memberId,
            @Param("status") String status
    );
    
    // 회원 상세 조회
    EvAdminMemberDetailDTO findMemberDetail(Long memberId);
    
    List<EvAdminMemberVehicleDTO> findMemberVehicleList(Long memberId);

    List<EvAdminMemberReservationDTO> findMemberReservationList(Long memberId);

    List<EvAdminMemberChargingDTO> findMemberChargingList(Long memberId);

    List<EvAdminMemberPaymentDTO> findMemberPaymentList(Long memberId);
}