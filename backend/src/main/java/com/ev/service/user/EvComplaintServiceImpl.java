package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ev.dao.user.EvComplaintDAO;
import com.ev.dto.complaint.EvComplaintAssignRequestDTO;
import com.ev.dto.complaint.EvComplaintDTO;
import com.ev.dto.complaint.EvComplaintRequestDTO;
import com.ev.dto.complaint.EvComplaintSearchDTO;
import com.ev.dto.complaint.EvComplaintStatusRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 민원 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvComplaintServiceImpl implements EvComplaintService {

    private final EvComplaintDAO evComplaintDAO;

    @Override
    @Transactional
    public Long createComplaint(Long memberId, EvComplaintRequestDTO requestDTO) {

        log.info("@# EvComplaintServiceImpl.createComplaint()");
        log.info("@# memberId => {}", memberId);
        log.info("@# requestDTO => {}", requestDTO);

        validateCreateRequest(requestDTO);

        requestDTO.setTitle(requestDTO.getTitle().trim());
        requestDTO.setContent(requestDTO.getContent().trim());
        requestDTO.setComplaintType(defaultString(requestDTO.getComplaintType(), "이용문의"));
        requestDTO.setPriority(defaultString(requestDTO.getPriority(), "NORMAL"));
        requestDTO.setNotifyEmail(safeBoolean(requestDTO.getNotifyEmail()));
        requestDTO.setNotifySms(safeBoolean(requestDTO.getNotifySms()));
        requestDTO.setNotifySite(requestDTO.getNotifySite() == null ? true : requestDTO.getNotifySite());

        requestDTO.setMemberId(memberId);

        evComplaintDAO.insertComplaint(requestDTO);

        Long complaintId = requestDTO.getComplaintId();

        evComplaintDAO.insertComplaintHistory(
                complaintId,
                null,
                null,
                "접수",
                "민원접수",
                "사용자가 민원을 접수했습니다."
        );

        log.info("@# complaint created complaintId => {}", complaintId);

        return complaintId;
    }

    @Override
    public List<EvComplaintDTO> getMyComplaintList(Long memberId) {
        log.info("@# EvComplaintServiceImpl.getMyComplaintList()");
        log.info("@# memberId => {}", memberId);

        return evComplaintDAO.findMyComplaintList(memberId);
    }

    @Override
    public EvComplaintDTO getMyComplaintDetail(Long memberId, Long complaintId) {
        log.info("@# EvComplaintServiceImpl.getMyComplaintDetail()");
        log.info("@# memberId => {}, complaintId => {}", memberId, complaintId);

        EvComplaintDTO complaintDTO = evComplaintDAO.findMyComplaintDetail(memberId, complaintId);

        if (complaintDTO == null) {
            throw new IllegalArgumentException("민원 정보를 찾을 수 없습니다.");
        }

        complaintDTO.setHistoryList(evComplaintDAO.findComplaintHistoryList(complaintId));

        return complaintDTO;
    }

    @Override
    public List<EvComplaintDTO> getAdminComplaintList(EvComplaintSearchDTO searchDTO) {
        log.info("@# EvComplaintServiceImpl.getAdminComplaintList()");
        log.info("@# searchDTO => {}", searchDTO);

        return evComplaintDAO.findAdminComplaintList(searchDTO);
    }

    @Override
    public EvComplaintDTO getAdminComplaintDetail(Long complaintId) {
        log.info("@# EvComplaintServiceImpl.getAdminComplaintDetail()");
        log.info("@# complaintId => {}", complaintId);

        EvComplaintDTO complaintDTO = evComplaintDAO.findAdminComplaintDetail(complaintId);

        if (complaintDTO == null) {
            throw new IllegalArgumentException("민원 정보를 찾을 수 없습니다.");
        }

        complaintDTO.setHistoryList(evComplaintDAO.findComplaintHistoryList(complaintId));

        return complaintDTO;
    }

    @Override
    @Transactional
    public void updateComplaintStatus(Long adminMemberId, Long complaintId, EvComplaintStatusRequestDTO requestDTO) {

        log.info("@# EvComplaintServiceImpl.updateComplaintStatus()");
        log.info("@# adminMemberId => {}, complaintId => {}, requestDTO => {}", adminMemberId, complaintId, requestDTO);

        if (requestDTO == null || !StringUtils.hasText(requestDTO.getStatus())) {
            throw new IllegalArgumentException("변경할 민원 상태를 선택해 주세요.");
        }

        EvComplaintDTO before = getAdminComplaintDetail(complaintId);
        Long employeeId = evComplaintDAO.findEmployeeIdByMemberId(adminMemberId);
        boolean closed = "완료".equals(requestDTO.getStatus()) || "반려".equals(requestDTO.getStatus()) || "취소".equals(requestDTO.getStatus());

        evComplaintDAO.updateComplaintStatus(
                complaintId,
                requestDTO.getStatus(),
                requestDTO.getAdminMemo(),
                closed
        );

        evComplaintDAO.insertComplaintHistory(
                complaintId,
                employeeId,
                before.getStatus(),
                requestDTO.getStatus(),
                "상태변경",
                requestDTO.getMemo()
        );
    }

    @Override
    @Transactional
    public void assignComplaint(Long adminMemberId, Long complaintId, EvComplaintAssignRequestDTO requestDTO) {

        log.info("@# EvComplaintServiceImpl.assignComplaint()");
        log.info("@# adminMemberId => {}, complaintId => {}, requestDTO => {}", adminMemberId, complaintId, requestDTO);

        if (requestDTO == null || requestDTO.getAssignedEmployeeId() == null) {
            throw new IllegalArgumentException("담당 직원을 선택해 주세요.");
        }

        EvComplaintDTO before = getAdminComplaintDetail(complaintId);
        Long employeeId = evComplaintDAO.findEmployeeIdByMemberId(adminMemberId);

        evComplaintDAO.updateComplaintAssign(
                complaintId,
                requestDTO.getAssignedDepartmentId(),
                requestDTO.getAssignedEmployeeId()
        );

        String afterStatus = "접수".equals(before.getStatus()) ? "배정" : before.getStatus();

        if (!before.getStatus().equals(afterStatus)) {
            evComplaintDAO.updateComplaintStatus(complaintId, afterStatus, null, false);
        }

        evComplaintDAO.insertComplaintHistory(
                complaintId,
                employeeId,
                before.getStatus(),
                afterStatus,
                "담당자배정",
                requestDTO.getMemo()
        );
    }

    private void validateCreateRequest(EvComplaintRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("민원 내용을 입력해 주세요.");
        }

        if (!StringUtils.hasText(requestDTO.getTitle())) {
            throw new IllegalArgumentException("민원 제목을 입력해 주세요.");
        }

        if (!StringUtils.hasText(requestDTO.getContent())) {
            throw new IllegalArgumentException("민원 내용을 입력해 주세요.");
        }
    }

    private String defaultString(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }

        return value;
    }

    private Boolean safeBoolean(Boolean value) {
        if (value == null) {
            return false;
        }

        return value;
    }

}
