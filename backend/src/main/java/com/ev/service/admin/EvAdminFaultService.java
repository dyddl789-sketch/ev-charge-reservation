package com.ev.service.admin;

import java.util.List;
import java.util.Map;

import com.ev.dto.admin.employee.EvAdminEmployeeDTO;
import com.ev.dto.admin.fault.EvAdminActionCompleteRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultAssignRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultDTO;
import com.ev.dto.admin.fault.EvAdminFaultSearchDTO;
import com.ev.dto.admin.fault.EvAdminInspectionResultRequestDTO;

/*
 * 관리자 장애·점검 Service
 */
public interface EvAdminFaultService {

    List<EvAdminFaultDTO> getFaultList(String status, String severity, String keyword);

    Map<String, Object> getFaultPage(Long requesterMemberId, EvAdminFaultSearchDTO searchDTO);

    EvAdminFaultDTO getFaultDetail(Long faultId);

    List<EvAdminEmployeeDTO> getEngineerList();

    EvAdminFaultDTO assignFault(Long adminMemberId, Long faultId, EvAdminFaultAssignRequestDTO requestDTO);

    EvAdminFaultDTO cancelFault(Long adminMemberId, Long faultId);

    EvAdminFaultDTO startInspection(Long adminMemberId, Long faultId);

    EvAdminFaultDTO saveInspectionResult(Long adminMemberId, Long faultId, EvAdminInspectionResultRequestDTO requestDTO);

    EvAdminFaultDTO completeAction(Long adminMemberId, Long faultId, EvAdminActionCompleteRequestDTO requestDTO);
}
