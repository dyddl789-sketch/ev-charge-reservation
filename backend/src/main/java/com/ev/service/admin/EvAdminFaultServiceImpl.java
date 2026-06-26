package com.ev.service.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ev.dao.admin.EvAdminFaultDAO;
import com.ev.dto.admin.employee.EvAdminEmployeeDTO;
import com.ev.dto.admin.fault.EvAdminActionCompleteRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultAssignRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultDTO;
import com.ev.dto.admin.fault.EvAdminFaultSearchDTO;
import com.ev.dto.admin.fault.EvAdminInspectionResultRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 장애·점검 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminFaultServiceImpl implements EvAdminFaultService {

    private final EvAdminFaultDAO evAdminFaultDAO;

    private static final Set<String> INSPECTION_RESULT_SET = Set.of("정상", "조치필요", "교체필요");

    @Override
    public List<EvAdminFaultDTO> getFaultList(String status, String severity, String keyword) {
        log.info("@# EvAdminFaultServiceImpl.getFaultList()");
        log.info("@# status => {}, severity => {}, keyword => {}", status, severity, keyword);

        EvAdminFaultSearchDTO searchDTO = new EvAdminFaultSearchDTO();
        searchDTO.setStatus(emptyToNull(status));
        searchDTO.setKeyword(emptyToNull(keyword));
        searchDTO.setPage(1);
        searchDTO.setSize(100);
        return evAdminFaultDAO.findFaultList(searchDTO);
    }

    @Override
    public Map<String, Object> getFaultPage(Long requesterMemberId, EvAdminFaultSearchDTO searchDTO) {
        log.info("@# EvAdminFaultServiceImpl.getFaultPage()");
        log.info("@# requesterMemberId => {}, searchDTO => {}", requesterMemberId, searchDTO);

        normalizeSearch(searchDTO);
        String role = requireRole(requesterMemberId, "ADMIN", "MANAGER", "ENGINEER");
        Long requesterEmployeeId = findAdminEmployeeId(requesterMemberId);

        searchDTO.setRequesterRole(role);
        searchDTO.setRequesterEmployeeId(requesterEmployeeId);

        List<EvAdminFaultDTO> list = evAdminFaultDAO.findFaultList(searchDTO);
        for (EvAdminFaultDTO faultDTO : list) {
            faultDTO.setHistoryList(evAdminFaultDAO.findFaultHistoryList(faultDTO.getFaultId()));
        }

        int totalCount = evAdminFaultDAO.countFaultList(searchDTO);
        int size = searchDTO.getLimit();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        Map<String, Object> summary = evAdminFaultDAO.countFaultSummary(searchDTO);

        Map<String, Object> result = new HashMap<>();
        result.put("items", list);
        result.put("list", list);
        result.put("page", searchDTO.getPage() < 1 ? 1 : searchDTO.getPage());
        result.put("size", size);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("summary", summary);

        return result;
    }

    @Override
    public EvAdminFaultDTO getFaultDetail(Long faultId) {
        log.info("@# EvAdminFaultServiceImpl.getFaultDetail()");
        log.info("@# faultId => {}", faultId);

        EvAdminFaultDTO faultDTO = evAdminFaultDAO.findFaultDetail(faultId);
        if (faultDTO == null) {
            throw new IllegalArgumentException("장애 정보를 찾을 수 없습니다.");
        }

        faultDTO.setHistoryList(evAdminFaultDAO.findFaultHistoryList(faultId));
        return faultDTO;
    }

    @Override
    public List<EvAdminEmployeeDTO> getEngineerList() {
        log.info("@# EvAdminFaultServiceImpl.getEngineerList()");
        return evAdminFaultDAO.findEngineerList();
    }

    @Override
    @Transactional
    public EvAdminFaultDTO assignFault(Long adminMemberId, Long faultId, EvAdminFaultAssignRequestDTO requestDTO) {
        log.info("@# EvAdminFaultServiceImpl.assignFault()");
        log.info("@# adminMemberId => {}, faultId => {}, requestDTO => {}", adminMemberId, faultId, requestDTO);

        requireRole(adminMemberId, "ADMIN", "MANAGER");
        EvAdminFaultDTO faultDTO = validateFaultExists(faultId);

        if (!"접수".equals(faultDTO.getStatus())) {
            throw new IllegalArgumentException("접수 상태의 장애만 담당자 배정이 가능합니다.");
        }

        if (requestDTO == null || requestDTO.getEmployeeId() == null) {
            throw new IllegalArgumentException("배정할 시설관리담당자를 선택해 주세요.");
        }

        Long adminEmployeeId = findAdminEmployeeId(adminMemberId);
        String beforeStatus = faultDTO.getStatus();

        evAdminFaultDAO.updateFaultAssignee(faultId, requestDTO.getEmployeeId());
        evAdminFaultDAO.insertFaultHistory(
                faultId,
                adminEmployeeId,
                beforeStatus,
                beforeStatus,
                "담당자배정",
                defaultMemo(requestDTO.getMemo(), "시설관리담당자가 배정되었습니다.")
        );

        log.info("@# fault assigned faultId => {}, employeeId => {}", faultId, requestDTO.getEmployeeId());

        return getFaultDetail(faultId);
    }

    @Override
    @Transactional
    public EvAdminFaultDTO cancelFault(Long adminMemberId, Long faultId) {
        log.info("@# EvAdminFaultServiceImpl.cancelFault()");
        log.info("@# adminMemberId => {}, faultId => {}", adminMemberId, faultId);

        requireRole(adminMemberId, "ADMIN", "MANAGER");
        EvAdminFaultDTO faultDTO = validateFaultExists(faultId);

        if (!"접수".equals(faultDTO.getStatus())) {
            throw new IllegalArgumentException("접수 상태의 장애만 접수취소할 수 있습니다.");
        }

        Long adminEmployeeId = findAdminEmployeeId(adminMemberId);
        evAdminFaultDAO.updateFaultStatus(faultId, "취소", true);
        evAdminFaultDAO.updateChargerStatusByFaultId(faultId, "사용가능");
        evAdminFaultDAO.updateLinkedComplaintStatus(faultId, "완료", true, "장애 접수가 취소되어 민원이 완료 처리되었습니다.");
        evAdminFaultDAO.insertFaultHistory(
                faultId,
                adminEmployeeId,
                "접수",
                "취소",
                "접수취소",
                "운영관리자가 장애 접수를 취소했습니다."
        );

        return getFaultDetail(faultId);
    }

    @Override
    @Transactional
    public EvAdminFaultDTO startInspection(Long adminMemberId, Long faultId) {
        log.info("@# EvAdminFaultServiceImpl.startInspection()");
        log.info("@# adminMemberId => {}, faultId => {}", adminMemberId, faultId);

        EvAdminFaultDTO faultDTO = validateFaultExists(faultId);
        Long adminEmployeeId = findAdminEmployeeId(adminMemberId);
        requireEngineerOwnerOrAdmin(adminMemberId, faultDTO, "점검 시작");

        Long inspectorId = faultDTO.getAssignedEmployeeId();
        String beforeStatus = faultDTO.getStatus();

        // 점검 시작은 최초 접수 상태에서만 허용한다.
        // 최종 결재 후 조치중 상태에서 다시 점검 시작을 누르면 장애 흐름이 역행하므로 서버에서 차단한다.
        if (!"접수".equals(beforeStatus)) {
            throw new IllegalArgumentException("접수 상태의 장애만 점검을 시작할 수 있습니다. 현재 상태: " + beforeStatus);
        }

        if (inspectorId == null) {
            throw new IllegalArgumentException("점검 시작 전 시설관리담당자를 먼저 배정해 주세요.");
        }

        evAdminFaultDAO.updateFaultStatusAndAssignee(faultId, "점검중", inspectorId, false);
        evAdminFaultDAO.updateChargerStatusByFaultId(faultId, "점검중");
        evAdminFaultDAO.updateLinkedComplaintStatus(faultId, "처리중", false, "장애 점검이 시작되었습니다.");

        Long inspectionId = evAdminFaultDAO.findLatestInspectionId(faultId);
        if (inspectionId == null) {
            evAdminFaultDAO.insertInspection(faultId, faultDTO.getChargerId(), inspectorId);
        }

        evAdminFaultDAO.insertFaultHistory(
                faultId,
                adminEmployeeId,
                beforeStatus,
                "점검중",
                "점검시작",
                "시설관리담당자가 충전기 점검을 시작했습니다."
        );

        log.info("@# inspection started faultId => {}, inspectorId => {}", faultId, inspectorId);

        return getFaultDetail(faultId);
    }

    @Override
    @Transactional
    public EvAdminFaultDTO saveInspectionResult(Long adminMemberId, Long faultId, EvAdminInspectionResultRequestDTO requestDTO) {
        log.info("@# EvAdminFaultServiceImpl.saveInspectionResult()");
        log.info("@# adminMemberId => {}, faultId => {}, requestDTO => {}", adminMemberId, faultId, requestDTO);

        EvAdminFaultDTO faultDTO = validateFaultExists(faultId);
        Long adminEmployeeId = findAdminEmployeeId(adminMemberId);
        requireEngineerOwnerOrAdmin(adminMemberId, faultDTO, "점검 결과 등록");

        if (!"점검중".equals(faultDTO.getStatus())) {
            throw new IllegalArgumentException("점검중 상태의 장애만 점검 결과를 등록할 수 있습니다.");
        }

        if (requestDTO == null || !StringUtils.hasText(requestDTO.getInspectionResult())) {
            throw new IllegalArgumentException("점검 결과를 선택해 주세요.");
        }

        String inspectionResult = requestDTO.getInspectionResult().trim();
        if (!INSPECTION_RESULT_SET.contains(inspectionResult)) {
            throw new IllegalArgumentException("점검 결과는 정상, 조치필요, 교체필요 중 하나여야 합니다.");
        }

        String beforeStatus = faultDTO.getStatus();
        String description = defaultMemo(requestDTO.getDescription(), "점검 결과: " + inspectionResult);
        boolean actionRequired = "조치필요".equals(inspectionResult) || "교체필요".equals(inspectionResult);

        Long inspectionId = evAdminFaultDAO.findLatestInspectionId(faultId);
        if (inspectionId == null) {
            evAdminFaultDAO.insertInspectionByFault(
                    faultId,
                    adminEmployeeId,
                    inspectionResult,
                    description,
                    actionRequired,
                    true
            );
            inspectionId = evAdminFaultDAO.findLatestInspectionId(faultId);
        } else {
            evAdminFaultDAO.updateInspectionResult(inspectionId, inspectionResult, description, actionRequired);
        }

        String afterStatus;
        if ("정상".equals(inspectionResult)) {
            afterStatus = "완료";
            evAdminFaultDAO.updateFaultStatus(faultId, afterStatus, true);
            evAdminFaultDAO.updateChargerStatusByFaultId(faultId, "사용가능");
            evAdminFaultDAO.updateLinkedComplaintStatus(faultId, "완료", true, "점검 결과 정상으로 확인되어 처리가 완료되었습니다.");
        } else if ("조치필요".equals(inspectionResult)) {
            afterStatus = "조치중";
            evAdminFaultDAO.updateFaultStatus(faultId, afterStatus, false);
            evAdminFaultDAO.updateChargerStatusByFaultId(faultId, "점검중");
            evAdminFaultDAO.updateLinkedComplaintStatus(faultId, "처리중", false, "점검 결과 조치가 필요한 상태입니다.");

            if (evAdminFaultDAO.findLatestActionId(faultId) == null) {
                evAdminFaultDAO.insertMaintenanceAction(faultId, inspectionId, adminEmployeeId, "현장조치");
            }
        } else {
            afterStatus = "결재대기";
            evAdminFaultDAO.updateFaultStatus(faultId, afterStatus, false);
            evAdminFaultDAO.updateChargerStatusByFaultId(faultId, "고장");
            evAdminFaultDAO.updateLinkedComplaintStatus(faultId, "처리중", false, "점검 결과 교체가 필요하여 전자결재 대기 상태로 전환되었습니다.");
        }

        evAdminFaultDAO.insertFaultHistory(
                faultId,
                adminEmployeeId,
                beforeStatus,
                afterStatus,
                "점검결과등록",
                description
        );

        log.info("@# inspection result saved faultId => {}, result => {}, afterStatus => {}", faultId, inspectionResult, afterStatus);

        return getFaultDetail(faultId);
    }

    @Override
    @Transactional
    public EvAdminFaultDTO completeAction(Long adminMemberId, Long faultId, EvAdminActionCompleteRequestDTO requestDTO) {
        log.info("@# EvAdminFaultServiceImpl.completeAction()");
        log.info("@# adminMemberId => {}, faultId => {}, requestDTO => {}", adminMemberId, faultId, requestDTO);

        EvAdminFaultDTO faultDTO = validateFaultExists(faultId);
        Long adminEmployeeId = findAdminEmployeeId(adminMemberId);
        requireEngineerOwnerOrAdmin(adminMemberId, faultDTO, "조치 완료");

        Long actionId = evAdminFaultDAO.findLatestActionId(faultId);

        // 기존 데이터가 결재 완료 후 화면 갱신/버튼 오류로 결재대기나 점검중에 남아도
        // 최종승인 문서와 진행중 조치가 있으면 조치중으로 보정해서 완료 처리 흐름을 복구한다.
        if (!"조치중".equals(faultDTO.getStatus())) {
            boolean hasApprovedProgressAction = actionId != null
                    && faultDTO.getApprovalDocumentId() != null
                    && "최종승인".equals(faultDTO.getApprovalStatus());

            if (!hasApprovedProgressAction) {
                throw new IllegalArgumentException("조치중 상태의 장애만 조치 완료 처리할 수 있습니다. 현재 상태: " + faultDTO.getStatus());
            }

            log.info("@# repair fault status before complete action. faultId => {}, beforeStatus => {}", faultId, faultDTO.getStatus());
            evAdminFaultDAO.updateFaultStatus(faultId, "조치중", false);
            faultDTO.setStatus("조치중");
        }

        if (actionId == null) {
            throw new IllegalArgumentException("진행 중인 조치 작업이 없습니다.");
        }

        String actionResult = defaultMemo(
                requestDTO == null ? null : requestDTO.getActionResult(),
                "현장 조치가 완료되었습니다."
        );

        String beforeStatus = faultDTO.getStatus();

        evAdminFaultDAO.completeMaintenanceAction(actionId, actionResult);
        evAdminFaultDAO.updateFaultStatus(faultId, "완료", true);
        evAdminFaultDAO.updateChargerStatusByFaultId(faultId, "사용가능");
        evAdminFaultDAO.updateLinkedComplaintStatus(faultId, "완료", true, "장애 조치가 완료되었습니다.");
        evAdminFaultDAO.completeLinkedApprovalDocumentByFaultId(faultId);
        evAdminFaultDAO.insertFaultHistory(
                faultId,
                adminEmployeeId,
                beforeStatus,
                "완료",
                "조치완료",
                actionResult
        );

        log.info("@# maintenance action completed faultId => {}, actionId => {}", faultId, actionId);

        return getFaultDetail(faultId);
    }

    private EvAdminFaultDTO validateFaultExists(Long faultId) {
        if (faultId == null) {
            throw new IllegalArgumentException("장애 번호가 없습니다.");
        }

        EvAdminFaultDTO faultDTO = evAdminFaultDAO.findFaultDetail(faultId);
        if (faultDTO == null) {
            throw new IllegalArgumentException("장애 정보를 찾을 수 없습니다.");
        }

        return faultDTO;
    }

    private String requireRole(Long memberId, String... allowedRoles) {
        String userType = evAdminFaultDAO.findUserTypeByMemberId(memberId);
        if (!StringUtils.hasText(userType)) {
            throw new IllegalArgumentException("권한 정보를 확인할 수 없습니다.");
        }
        for (String role : allowedRoles) {
            if (role.equals(userType)) {
                return userType;
            }
        }
        throw new IllegalArgumentException("현재 권한으로 처리할 수 없는 업무입니다.");
    }

    private void requireEngineerOwnerOrAdmin(Long memberId, EvAdminFaultDTO faultDTO, String actionName) {
        String role = requireRole(memberId, "ADMIN", "ENGINEER");
        if ("ADMIN".equals(role)) {
            return;
        }

        Long employeeId = findAdminEmployeeId(memberId);
        if (faultDTO.getAssignedEmployeeId() == null || !faultDTO.getAssignedEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("본인에게 배정된 장애만 " + actionName + " 처리할 수 있습니다.");
        }
    }

    private Long findAdminEmployeeId(Long adminMemberId) {
        Long employeeId = evAdminFaultDAO.findEmployeeIdByMemberId(adminMemberId);
        if (employeeId == null) {
            throw new IllegalArgumentException("직원 정보가 등록된 관리자만 처리할 수 있습니다.");
        }

        return employeeId;
    }

    private void normalizeSearch(EvAdminFaultSearchDTO searchDTO) {
        searchDTO.setStatus(emptyToNull(searchDTO.getStatus()));
        searchDTO.setKeyword(emptyToNull(searchDTO.getKeyword()));
        searchDTO.setReportedFrom(emptyToNull(searchDTO.getReportedFrom()));
        searchDTO.setReportedTo(emptyToNull(searchDTO.getReportedTo()));
        searchDTO.setResolvedFrom(emptyToNull(searchDTO.getResolvedFrom()));
        searchDTO.setResolvedTo(emptyToNull(searchDTO.getResolvedTo()));
        if (searchDTO.getPage() < 1) {
            searchDTO.setPage(1);
        }
        if (searchDTO.getSize() < 1) {
            searchDTO.setSize(10);
        }
        if (searchDTO.getSize() > 100) {
            searchDTO.setSize(100);
        }
    }

    private String defaultMemo(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }

        return value.trim();
    }

    private String emptyToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
