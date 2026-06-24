package com.ev.service.user;

import java.util.List;

import com.ev.dto.complaint.EvComplaintAssignRequestDTO;
import com.ev.dto.complaint.EvComplaintDTO;
import com.ev.dto.complaint.EvComplaintRequestDTO;
import com.ev.dto.complaint.EvComplaintSearchDTO;
import com.ev.dto.complaint.EvComplaintStatusRequestDTO;

public interface EvComplaintService {

    // 사용자 민원 등록
    Long createComplaint(Long memberId, EvComplaintRequestDTO requestDTO);

    // 내 민원 목록 조회
    List<EvComplaintDTO> getMyComplaintList(Long memberId);

    // 내 민원 상세 조회
    EvComplaintDTO getMyComplaintDetail(Long memberId, Long complaintId);

    // 관리자 민원 목록 조회
    List<EvComplaintDTO> getAdminComplaintList(EvComplaintSearchDTO searchDTO);

    // 관리자 민원 상세 조회
    EvComplaintDTO getAdminComplaintDetail(Long complaintId);

    // 관리자 민원 상태 변경
    void updateComplaintStatus(Long adminMemberId, Long complaintId, EvComplaintStatusRequestDTO requestDTO);

    // 관리자 민원 담당자 배정
    void assignComplaint(Long adminMemberId, Long complaintId, EvComplaintAssignRequestDTO requestDTO);
}
