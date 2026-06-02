package com.ev.service.admin;

import com.ev.dto.admin.EvAdminMemberPageDTO;
import com.ev.dto.admin.EvAdminMemberPaymentDTO;
import com.ev.dto.admin.EvAdminMemberReservationDTO;
import com.ev.dto.admin.EvAdminMemberSearchDTO;
import com.ev.dto.admin.EvAdminMemberVehicleDTO;

import java.util.List;

import com.ev.dto.admin.EvAdminMemberChargingDTO;
import com.ev.dto.admin.EvAdminMemberDetailDTO;
public interface EvAdminMemberService {

    EvAdminMemberPageDTO getMemberPage(EvAdminMemberSearchDTO searchDTO);

    void withdrawMember(Long memberId);

    void restoreMember(Long memberId);
    
    EvAdminMemberDetailDTO getMemberDetail(Long memberId);
    
    List<EvAdminMemberVehicleDTO> getMemberVehicleList(Long memberId);

    List<EvAdminMemberReservationDTO> getMemberReservationList(Long memberId);

    List<EvAdminMemberChargingDTO> getMemberChargingList(Long memberId);

    List<EvAdminMemberPaymentDTO> getMemberPaymentList(Long memberId);
}