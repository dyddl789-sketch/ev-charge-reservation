package com.ev.service.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ev.dao.admin.EvAdminApprovalDAO;
import com.ev.dao.admin.EvAdminFaultDAO;
import com.ev.dto.admin.approval.EvAdminApprovalDTO;
import com.ev.dto.admin.approval.EvAdminApprovalDecisionRequestDTO;
import com.ev.dto.admin.approval.EvAdminApprovalLineDTO;
import com.ev.dto.admin.approval.EvAdminApprovalSubmitRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 전자결재 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminApprovalServiceImpl implements EvAdminApprovalService {

    private final EvAdminApprovalDAO evAdminApprovalDAO;
    private final EvAdminFaultDAO evAdminFaultDAO;

    private static final Set<String> CLOSED_STATUS_SET = Set.of("최종승인", "완료", "반려", "취소");

    @Override
    public List<EvAdminApprovalDTO> getApprovalList(Long memberId, String userType, String box, String status, String keyword) {
        log.info("@# EvAdminApprovalServiceImpl.getApprovalList()");
        log.info("@# memberId => {}, userType => {}, box => {}, status => {}, keyword => {}", memberId, userType, box, status, keyword);

        Long employeeId = findEmployeeId(memberId);
        return evAdminApprovalDAO.findApprovalList(emptyToNull(box), emptyToNull(status), emptyToNull(keyword), employeeId, userType);
    }

    @Override
    public EvAdminApprovalDTO getApprovalDetail(Long documentId) {
        log.info("@# EvAdminApprovalServiceImpl.getApprovalDetail()");
        log.info("@# documentId => {}", documentId);

        EvAdminApprovalDTO approvalDTO = evAdminApprovalDAO.findApprovalDetail(documentId);
        if (approvalDTO == null) {
            throw new IllegalArgumentException("전자결재 문서를 찾을 수 없습니다.");
        }

        approvalDTO.setLineList(evAdminApprovalDAO.findApprovalLineList(documentId));
        approvalDTO.setHistoryList(evAdminApprovalDAO.findApprovalHistoryList(documentId));
        return approvalDTO;
    }

    @Override
    @Transactional
    public EvAdminApprovalDTO submitFaultApproval(Long memberId, Long faultId, EvAdminApprovalSubmitRequestDTO requestDTO) {
        log.info("@# EvAdminApprovalServiceImpl.submitFaultApproval()");
        log.info("@# memberId => {}, faultId => {}, requestDTO => {}", memberId, faultId, requestDTO);

        Long writerEmployeeId = findEmployeeId(memberId);
        EvAdminFaultDTO faultDTO = evAdminFaultDAO.findFaultDetail(faultId);
        if (faultDTO == null) {
            throw new IllegalArgumentException("장애 정보를 찾을 수 없습니다.");
        }

        if (!"결재대기".equals(faultDTO.getStatus())) {
            throw new IllegalArgumentException("결재대기 상태의 장애만 전자결재를 상신할 수 있습니다.");
        }

        if (!"교체필요".equals(faultDTO.getLatestInspectionResult())) {
            throw new IllegalArgumentException("점검 결과가 교체필요인 장애만 전자결재를 상신할 수 있습니다.");
        }

        if (evAdminApprovalDAO.countOpenApprovalByFaultId(faultId) > 0) {
            throw new IllegalArgumentException("이미 진행 중인 전자결재 문서가 있습니다.");
        }

        Long managerEmployeeId = evAdminApprovalDAO.findFirstActiveEmployeeIdByUserType("MANAGER");
        Long adminEmployeeId = evAdminApprovalDAO.findFirstActiveEmployeeIdByUserType("ADMIN");

        if (managerEmployeeId == null) {
            throw new IllegalArgumentException("1차 승인자인 운영관리자(MANAGER) 직원이 없습니다.");
        }

        if (adminEmployeeId == null) {
            throw new IllegalArgumentException("최종 승인자인 기관장/최고관리자(ADMIN) 직원이 없습니다.");
        }

        EvAdminApprovalDTO documentDTO = new EvAdminApprovalDTO();
        documentDTO.setWriterId(writerEmployeeId);
        documentDTO.setFaultId(faultId);
        documentDTO.setInspectionId(faultDTO.getLatestInspectionId());
        documentDTO.setDocumentType(defaultText(requestDTO == null ? null : requestDTO.getDocumentType(), "충전기 교체 요청서"));
        documentDTO.setTitle(defaultText(requestDTO == null ? null : requestDTO.getTitle(), buildDefaultTitle(faultDTO)));
        documentDTO.setContent(defaultText(requestDTO == null ? null : requestDTO.getContent(), buildDefaultContent(faultDTO)));
        documentDTO.setEstimatedCost(requestDTO == null || requestDTO.getEstimatedCost() == null ? BigDecimal.ZERO : requestDTO.getEstimatedCost());
        documentDTO.setStatus("상신");

        evAdminApprovalDAO.insertApprovalDocument(documentDTO);
        Long documentId = documentDTO.getDocumentId();

        evAdminApprovalDAO.insertApprovalLine(documentId, managerEmployeeId, 1);
        evAdminApprovalDAO.insertApprovalLine(documentId, adminEmployeeId, 2);
        evAdminApprovalDAO.insertApprovalHistory(documentId, writerEmployeeId, "상신", null, "상신", "교체필요 점검 결과에 따라 전자결재 문서를 상신했습니다.");

        evAdminFaultDAO.insertFaultHistory(
                faultId,
                writerEmployeeId,
                faultDTO.getStatus(),
                faultDTO.getStatus(),
                "전자결재상신",
                "교체 요청 전자결재 문서를 상신했습니다."
        );

        log.info("@# approval submitted documentId => {}, faultId => {}", documentId, faultId);

        return getApprovalDetail(documentId);
    }

    @Override
    @Transactional
    public EvAdminApprovalDTO approveApproval(Long memberId, String userType, Long documentId, EvAdminApprovalDecisionRequestDTO requestDTO) {
        log.info("@# EvAdminApprovalServiceImpl.approveApproval()");
        log.info("@# memberId => {}, userType => {}, documentId => {}, requestDTO => {}", memberId, userType, documentId, requestDTO);

        Long approverEmployeeId = findEmployeeId(memberId);
        EvAdminApprovalDTO approvalDTO = getApprovalDetail(documentId);

        if (CLOSED_STATUS_SET.contains(approvalDTO.getStatus()) || "반려".equals(approvalDTO.getStatus())) {
            throw new IllegalArgumentException("이미 처리 완료된 전자결재 문서입니다.");
        }

        String signatureData = requestDTO == null ? null : requestDTO.getSignatureData();
        if (!StringUtils.hasText(signatureData)) {
            throw new IllegalArgumentException("승인 전자서명을 입력해 주세요.");
        }

        EvAdminApprovalLineDTO pendingLine = findCurrentPendingLine(approvalDTO);
        validateApprover(approverEmployeeId, pendingLine);
        validateApprovalRole(userType, pendingLine);

        String beforeStatus = approvalDTO.getStatus();
        String comment = defaultText(requestDTO == null ? null : requestDTO.getComment(), "확인 후 승인합니다.");
        String afterStatus = pendingLine.getApprovalOrder() != null && pendingLine.getApprovalOrder() == 1 ? "1차승인" : "최종승인";

        evAdminApprovalDAO.updateApprovalLineDecision(pendingLine.getLineId(), "승인", comment, signatureData);
        evAdminApprovalDAO.updateApprovalDocumentStatus(documentId, afterStatus, "최종승인".equals(afterStatus));
        evAdminApprovalDAO.insertApprovalHistory(documentId, approverEmployeeId, afterStatus, beforeStatus, afterStatus, comment);

        if ("최종승인".equals(afterStatus)) {
            applyFinalApprovalSideEffects(documentId, approverEmployeeId, beforeStatus);
        }

        log.info("@# approval approved documentId => {}, afterStatus => {}", documentId, afterStatus);

        return getApprovalDetail(documentId);
    }

    @Override
    @Transactional
    public EvAdminApprovalDTO rejectApproval(Long memberId, String userType, Long documentId, EvAdminApprovalDecisionRequestDTO requestDTO) {
        log.info("@# EvAdminApprovalServiceImpl.rejectApproval()");
        log.info("@# memberId => {}, userType => {}, documentId => {}, requestDTO => {}", memberId, userType, documentId, requestDTO);

        Long approverEmployeeId = findEmployeeId(memberId);
        EvAdminApprovalDTO approvalDTO = getApprovalDetail(documentId);

        if (CLOSED_STATUS_SET.contains(approvalDTO.getStatus()) || "반려".equals(approvalDTO.getStatus())) {
            throw new IllegalArgumentException("이미 처리 완료된 전자결재 문서입니다.");
        }

        String comment = requestDTO == null ? null : requestDTO.getComment();
        if (!StringUtils.hasText(comment)) {
            throw new IllegalArgumentException("반려 사유를 입력해 주세요.");
        }

        EvAdminApprovalLineDTO pendingLine = findCurrentPendingLine(approvalDTO);
        validateApprover(approverEmployeeId, pendingLine);
        validateApprovalRole(userType, pendingLine);

        String beforeStatus = approvalDTO.getStatus();
        String signatureData = requestDTO == null ? null : requestDTO.getSignatureData();

        evAdminApprovalDAO.updateApprovalLineDecision(pendingLine.getLineId(), "반려", comment.trim(), signatureData);
        evAdminApprovalDAO.updateApprovalDocumentStatus(documentId, "반려", true);
        evAdminApprovalDAO.insertApprovalHistory(documentId, approverEmployeeId, "반려", beforeStatus, "반려", comment.trim());
        evAdminApprovalDAO.insertFaultHistoryByDocument(
                documentId,
                approverEmployeeId,
                "결재대기",
                "결재대기",
                "전자결재반려",
                comment.trim()
        );

        log.info("@# approval rejected documentId => {}", documentId);

        return getApprovalDetail(documentId);
    }

    private Long findEmployeeId(Long memberId) {
        Long employeeId = evAdminApprovalDAO.findEmployeeIdByMemberId(memberId);
        if (employeeId == null) {
            throw new IllegalArgumentException("직원 정보가 연결된 관리자 계정만 전자결재를 사용할 수 있습니다.");
        }

        return employeeId;
    }

    private EvAdminApprovalLineDTO findCurrentPendingLine(EvAdminApprovalDTO approvalDTO) {
        if (approvalDTO.getLineList() == null) {
            throw new IllegalArgumentException("결재선 정보를 찾을 수 없습니다.");
        }

        return approvalDTO.getLineList()
                .stream()
                .filter(line -> "대기".equals(line.getStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("현재 결재 대기자가 없습니다."));
    }

    private void validateApprover(Long approverEmployeeId, EvAdminApprovalLineDTO pendingLine) {
        if (pendingLine == null || pendingLine.getApproverId() == null || !pendingLine.getApproverId().equals(approverEmployeeId)) {
            throw new IllegalArgumentException("현재 결재 차례인 담당자만 처리할 수 있습니다.");
        }
    }

    private void validateApprovalRole(String userType, EvAdminApprovalLineDTO pendingLine) {
        String role = userType == null ? "" : userType.replace("ROLE_", "").toUpperCase();
        Integer order = pendingLine.getApprovalOrder();

        if (order != null && order == 1 && !"MANAGER".equals(role)) {
            throw new IllegalArgumentException("운영관리자만 1차 승인을 처리할 수 있습니다.");
        }

        if (order != null && order == 2 && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("기관장/최고관리자만 최종 승인을 처리할 수 있습니다.");
        }
    }

    private void applyFinalApprovalSideEffects(Long documentId, Long approverEmployeeId, String beforeStatus) {
        log.info("@# EvAdminApprovalServiceImpl.applyFinalApprovalSideEffects()");
        log.info("@# documentId => {}, approverEmployeeId => {}", documentId, approverEmployeeId);

        evAdminApprovalDAO.updateFaultStatusByDocumentId(documentId, "조치중", false);
        evAdminApprovalDAO.updateLinkedComplaintStatusByDocumentId(
                documentId,
                "처리중",
                false,
                "전자결재 최종 승인으로 교체 작업이 진행됩니다."
        );

        if (evAdminApprovalDAO.countProgressMaintenanceActionByDocument(documentId) == 0) {
            evAdminApprovalDAO.insertMaintenanceActionByDocument(
                    documentId,
                    approverEmployeeId,
                    "교체작업",
                    "전자결재 최종 승인 후 교체 작업 진행 중"
            );
        }

        evAdminApprovalDAO.insertFaultHistoryByDocument(
                documentId,
                approverEmployeeId,
                "결재대기",
                "조치중",
                "전자결재최종승인",
                "전자결재 최종 승인으로 교체 작업 단계로 전환했습니다."
        );
    }

    private String buildDefaultTitle(EvAdminFaultDTO faultDTO) {
        return String.format("%s %s 교체 요청", nullToDash(faultDTO.getStationName()), nullToDash(faultDTO.getChargerName()));
    }

    private String buildDefaultContent(EvAdminFaultDTO faultDTO) {
        return String.format(
                "%s %s 점검 결과 %s로 확인되었습니다.\n\n장애 내용: %s\n점검 내용: %s\n\n정상 운영을 위해 부품 교체 또는 장비 교체 승인을 요청합니다.",
                nullToDash(faultDTO.getStationName()),
                nullToDash(faultDTO.getChargerName()),
                nullToDash(faultDTO.getLatestInspectionResult()),
                nullToDash(faultDTO.getDescription()),
                nullToDash(faultDTO.getLatestInspectionDescription())
        );
    }

    private String defaultText(String value, String defaultValue) {
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

    private String nullToDash(String value) {
        if (!StringUtils.hasText(value)) {
            return "-";
        }

        return value;
    }
}
