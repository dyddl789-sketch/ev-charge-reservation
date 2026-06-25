package com.ev.service.admin;

import java.util.List;

import com.ev.dto.admin.approval.EvAdminApprovalDTO;
import com.ev.dto.admin.approval.EvAdminApprovalDecisionRequestDTO;
import com.ev.dto.admin.approval.EvAdminApprovalSubmitRequestDTO;

/*
 * 관리자 전자결재 Service
 */
public interface EvAdminApprovalService {

    List<EvAdminApprovalDTO> getApprovalList(Long memberId, String userType, String box, String status, String keyword);

    EvAdminApprovalDTO getApprovalDetail(Long documentId);

    EvAdminApprovalDTO submitFaultApproval(Long memberId, Long faultId, EvAdminApprovalSubmitRequestDTO requestDTO);

    EvAdminApprovalDTO approveApproval(Long memberId, String userType, Long documentId, EvAdminApprovalDecisionRequestDTO requestDTO);

    EvAdminApprovalDTO rejectApproval(Long memberId, String userType, Long documentId, EvAdminApprovalDecisionRequestDTO requestDTO);
}
