package com.ev.service.user;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ev.dao.user.EvComplaintDAO;
import com.ev.dto.complaint.EvComplaintAnswerRequestDTO;
import com.ev.dto.complaint.EvComplaintAssignRequestDTO;
import com.ev.dto.complaint.EvComplaintDTO;
import com.ev.dto.complaint.EvComplaintFaultRegisterRequestDTO;
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

    private static final String DEFAULT_FAULT_ANSWER = "신고해 주신 충전기 이상 내용을 확인했습니다.\n"
            + "해당 충전기는 장애점검관리로 접수되었으며, 시설관리담당자가 점검 및 수리를 진행할 예정입니다.\n"
            + "신속히 처리하겠습니다.";

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
        // SMS 알림은 화면 정책에서 제거했으므로 DB 컬럼은 유지하되 항상 false로 저장한다.
        requestDTO.setNotifySms(false);
        requestDTO.setNotifySite(requestDTO.getNotifySite() == null ? true : requestDTO.getNotifySite());
        requestDTO.setAiSummary(buildAiSummary(requestDTO.getComplaintType(), requestDTO.getTitle(), requestDTO.getContent()));

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

        List<EvComplaintDTO> list = evComplaintDAO.findMyComplaintList(memberId);
        hydrateAiFields(list);
        return list;
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
        hydrateAiFields(complaintDTO);

        return complaintDTO;
    }

    @Override
    public List<EvComplaintDTO> getAdminComplaintList(EvComplaintSearchDTO searchDTO) {
        log.info("@# EvComplaintServiceImpl.getAdminComplaintList()");
        log.info("@# searchDTO => {}", searchDTO);

        normalizeSearch(searchDTO);
        List<EvComplaintDTO> list = evComplaintDAO.findAdminComplaintList(searchDTO);
        hydrateAiFields(list);
        return list;
    }

    @Override
    public Map<String, Object> getAdminComplaintPage(EvComplaintSearchDTO searchDTO) {
        log.info("@# EvComplaintServiceImpl.getAdminComplaintPage()");
        log.info("@# searchDTO => {}", searchDTO);

        normalizeSearch(searchDTO);

        List<EvComplaintDTO> list = evComplaintDAO.findAdminComplaintList(searchDTO);
        hydrateAiFields(list);

        int totalCount = evComplaintDAO.countAdminComplaintList(searchDTO);
        int size = searchDTO.getLimit();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        Map<String, Object> summary = evComplaintDAO.countAdminComplaintSummary(searchDTO);

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
    public EvComplaintDTO getAdminComplaintDetail(Long complaintId) {
        log.info("@# EvComplaintServiceImpl.getAdminComplaintDetail()");
        log.info("@# complaintId => {}", complaintId);

        EvComplaintDTO complaintDTO = evComplaintDAO.findAdminComplaintDetail(complaintId);

        if (complaintDTO == null) {
            throw new IllegalArgumentException("민원 정보를 찾을 수 없습니다.");
        }

        complaintDTO.setHistoryList(evComplaintDAO.findComplaintHistoryList(complaintId));
        hydrateAiFields(complaintDTO);

        return complaintDTO;
    }

    @Override
    @Transactional
    public void updateComplaintStatus(Long adminMemberId, Long complaintId, EvComplaintStatusRequestDTO requestDTO) {

        log.info("@# EvComplaintServiceImpl.updateComplaintStatus()");
        log.info("@# adminMemberId => {}, complaintId => {}, requestDTO => {}", adminMemberId, complaintId, requestDTO);

        requireRole(adminMemberId, "ADMIN", "MANAGER", "OPERATOR");

        if (requestDTO == null || !StringUtils.hasText(requestDTO.getStatus())) {
            throw new IllegalArgumentException("변경할 민원 상태를 선택해 주세요.");
        }

        EvComplaintDTO before = getAdminComplaintDetail(complaintId);
        Long employeeId = findAdminEmployeeId(adminMemberId);
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

        requireRole(adminMemberId, "ADMIN", "MANAGER", "OPERATOR");

        if (requestDTO == null || requestDTO.getAssignedEmployeeId() == null) {
            throw new IllegalArgumentException("담당 직원을 선택해 주세요.");
        }

        EvComplaintDTO before = getAdminComplaintDetail(complaintId);
        Long employeeId = findAdminEmployeeId(adminMemberId);

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

    @Override
    @Transactional
    public void answerComplaint(Long adminMemberId, Long complaintId, EvComplaintAnswerRequestDTO requestDTO) {
        log.info("@# EvComplaintServiceImpl.answerComplaint()");
        log.info("@# adminMemberId => {}, complaintId => {}, requestDTO => {}", adminMemberId, complaintId, requestDTO);

        requireRole(adminMemberId, "ADMIN", "MANAGER", "OPERATOR");

        if (requestDTO == null || !StringUtils.hasText(requestDTO.getAnswerContent())) {
            throw new IllegalArgumentException("사용자에게 전달할 답변을 입력해 주세요.");
        }

        EvComplaintDTO before = getAdminComplaintDetail(complaintId);
        if ("완료".equals(before.getStatus())) {
            throw new IllegalArgumentException("이미 완료된 민원입니다.");
        }

        Long employeeId = findAdminEmployeeId(adminMemberId);
        String answerContent = requestDTO.getAnswerContent().trim();

        evComplaintDAO.updateComplaintAnswer(complaintId, answerContent);
        evComplaintDAO.insertComplaintHistory(
                complaintId,
                employeeId,
                before.getStatus(),
                "완료",
                "답변완료",
                defaultString(requestDTO.getMemo(), "운영담당자가 민원 답변을 완료했습니다.")
        );
    }

    @Override
    @Transactional
    public Long registerFaultFromComplaint(Long adminMemberId, Long complaintId, EvComplaintFaultRegisterRequestDTO requestDTO) {
        log.info("@# EvComplaintServiceImpl.registerFaultFromComplaint()");
        log.info("@# adminMemberId => {}, complaintId => {}, requestDTO => {}", adminMemberId, complaintId, requestDTO);

        requireRole(adminMemberId, "ADMIN", "MANAGER", "OPERATOR");

        EvComplaintDTO complaintDTO = getAdminComplaintDetail(complaintId);
        if ("완료".equals(complaintDTO.getStatus())) {
            throw new IllegalArgumentException("이미 완료된 민원입니다.");
        }
        if (complaintDTO.getStationId() == null || complaintDTO.getChargerId() == null) {
            throw new IllegalArgumentException("충전소와 충전기가 선택된 민원만 장애접수할 수 있습니다.");
        }
        if (evComplaintDAO.countFaultByComplaintId(complaintId) > 0) {
            throw new IllegalArgumentException("이미 장애점검관리로 접수된 민원입니다.");
        }

        Long employeeId = findAdminEmployeeId(adminMemberId);
        String faultType = defaultString(requestDTO == null ? null : requestDTO.getFaultType(), "충전기고장");
        String title = defaultString(requestDTO == null ? null : requestDTO.getTitle(), complaintDTO.getTitle());
        String description = defaultString(requestDTO == null ? null : requestDTO.getDescription(), complaintDTO.getContent());
        String answer = defaultString(requestDTO == null ? null : requestDTO.getAnswerContent(), DEFAULT_FAULT_ANSWER);

        evComplaintDAO.insertFaultFromComplaint(complaintId, faultType, title, description);
        evComplaintDAO.updateChargerStatusByComplaintId(complaintId, "점검중");
        evComplaintDAO.updateComplaintAnswer(complaintId, answer);

        Long faultId = evComplaintDAO.findFaultIdByComplaintId(complaintId);

        evComplaintDAO.insertComplaintHistory(
                complaintId,
                employeeId,
                complaintDTO.getStatus(),
                "완료",
                "장애접수",
                "장애점검관리로 접수되었습니다. 연결 장애번호 #" + faultId
        );

        log.info("@# complaint registered as fault complaintId => {}, faultId => {}", complaintId, faultId);
        return faultId;
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

    private void normalizeSearch(EvComplaintSearchDTO searchDTO) {
        if (searchDTO == null) {
            return;
        }
        searchDTO.setStatus(emptyToNull(searchDTO.getStatus()));
        searchDTO.setComplaintType(emptyToNull(searchDTO.getComplaintType()));
        searchDTO.setPriority(emptyToNull(searchDTO.getPriority()));
        searchDTO.setKeyword(emptyToNull(searchDTO.getKeyword()));
        searchDTO.setCreatedFrom(emptyToNull(searchDTO.getCreatedFrom()));
        searchDTO.setCreatedTo(emptyToNull(searchDTO.getCreatedTo()));
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

    private String buildAiSummary(String complaintType, String title, String content) {
        AiClassification classification = classify(complaintType, title, content);
        return classification.label() + "|" + classification.confidence() + "|" + classification.reason();
    }

    private void hydrateAiFields(List<EvComplaintDTO> list) {
        if (list == null) {
            return;
        }
        list.forEach(this::hydrateAiFields);
    }

    private void hydrateAiFields(EvComplaintDTO dto) {
        if (dto == null) {
            return;
        }

        if (!StringUtils.hasText(dto.getAnswerContent())) {
            dto.setAnswerContent(dto.getAdminMemo());
        }

        AiClassification classification = parseOrClassify(dto.getAiSummary(), dto.getComplaintType(), dto.getTitle(), dto.getContent());
        dto.setAiCategory(classification.category());
        dto.setAiLabel(classification.label());
        dto.setAiConfidence(classification.confidence());
        dto.setAiReason(classification.reason());
    }

    private AiClassification parseOrClassify(String aiSummary, String complaintType, String title, String content) {
        if (StringUtils.hasText(aiSummary) && aiSummary.contains("|")) {
            String[] parts = aiSummary.split("\\|", 3);
            String label = parts.length > 0 ? parts[0] : "일반민원";
            double confidence = 0.75;
            if (parts.length > 1) {
                try {
                    confidence = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    log.warn("@# ai confidence parse failed => {}", parts[1]);
                }
            }
            String reason = parts.length > 2 ? parts[2] : "민원 내용 기반 자동 분류 결과입니다.";
            String category = label.contains("고장") || label.contains("장애") ? "FAULT" : "GENERAL";
            return new AiClassification(category, label, confidence, reason);
        }
        return classify(complaintType, title, content);
    }

    private AiClassification classify(String complaintType, String title, String content) {
        String text = ((complaintType == null ? "" : complaintType) + " "
                + (title == null ? "" : title) + " "
                + (content == null ? "" : content)).toLowerCase(Locale.ROOT);

        if (containsAny(text, "고장", "파손", "충전이 안", "작동 안", "작동안", "인식 안", "전원", "에러", "오류", "통신", "케이블", "멈춤", "중단", "불량")) {
            return new AiClassification("FAULT", "고장 의심", 0.91, "고장, 작동 불가, 오류 등 시설장애 가능성이 높은 표현이 포함되어 있습니다.");
        }
        if (containsAny(text, "결제", "카드", "환불", "요금")) {
            return new AiClassification("GENERAL", "결제문의", 0.86, "결제, 환불, 요금 관련 문의로 분류되었습니다.");
        }
        if (containsAny(text, "예약", "취소", "변경", "시간")) {
            return new AiClassification("GENERAL", "예약문의", 0.84, "예약, 취소, 시간 변경 관련 문의로 분류되었습니다.");
        }
        if (containsAny(text, "회원", "로그인", "비밀번호", "차량등록", "차량 등록")) {
            return new AiClassification("GENERAL", "회원문의", 0.82, "회원정보 또는 차량 등록 관련 문의로 분류되었습니다.");
        }
        return new AiClassification("GENERAL", "일반민원", 0.76, "시설장애 키워드가 뚜렷하지 않아 일반 민원으로 분류되었습니다.");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void requireRole(Long memberId, String... allowedRoles) {
        String userType = evComplaintDAO.findUserTypeByMemberId(memberId);
        if (!StringUtils.hasText(userType)) {
            throw new IllegalArgumentException("권한 정보를 확인할 수 없습니다.");
        }
        for (String role : allowedRoles) {
            if (role.equals(userType)) {
                return;
            }
        }
        throw new IllegalArgumentException("현재 권한으로 처리할 수 없는 업무입니다.");
    }

    private Long findAdminEmployeeId(Long adminMemberId) {
        Long employeeId = evComplaintDAO.findEmployeeIdByMemberId(adminMemberId);
        if (employeeId == null) {
            throw new IllegalArgumentException("직원 정보가 등록된 관리자만 처리할 수 있습니다.");
        }
        return employeeId;
    }

    private String defaultString(String value, String defaultValue) {
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

    private Boolean safeBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    private record AiClassification(String category, String label, double confidence, String reason) {
    }
}
