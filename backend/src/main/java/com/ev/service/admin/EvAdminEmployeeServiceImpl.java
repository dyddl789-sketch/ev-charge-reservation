package com.ev.service.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ev.dao.admin.EvAdminEmployeeDAO;
import com.ev.dto.admin.employee.EvAdminDepartmentDTO;
import com.ev.dto.admin.employee.EvAdminEmployeeDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 인사관리 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminEmployeeServiceImpl implements EvAdminEmployeeService {

    private final EvAdminEmployeeDAO evAdminEmployeeDAO;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<EvAdminEmployeeDTO> getEmployees(String keyword,
                                                 Long departmentId,
                                                 String userType,
                                                 String status) {
        log.info("@# EvAdminEmployeeServiceImpl.getEmployees()");
        log.info("@# keyword => {}, departmentId => {}, userType => {}, status => {}",
                keyword, departmentId, userType, status);

        return evAdminEmployeeDAO.findEmployees(keyword, departmentId, userType, status);
    }

    @Override
    public EvAdminEmployeeDTO getEmployee(Long employeeId) {
        log.info("@# EvAdminEmployeeServiceImpl.getEmployee() employeeId => {}", employeeId);
        return evAdminEmployeeDAO.findEmployeeById(employeeId);
    }

    @Override
    public List<EvAdminDepartmentDTO> getDepartments() {
        log.info("@# EvAdminEmployeeServiceImpl.getDepartments()");
        return evAdminEmployeeDAO.findDepartments();
    }

    @Override
    @Transactional
    public EvAdminEmployeeDTO createEmployee(EvAdminEmployeeDTO requestDTO, String actorRole) {
        log.info("@# EvAdminEmployeeServiceImpl.createEmployee()");
        log.info("@# actorRole => {}, requestDTO => {}", actorRole, requestDTO);

        validateManagePermission(actorRole, requestDTO.getUserType());
        validateRequiredForCreate(requestDTO);
        validateDuplicate(requestDTO, null);

        if (requestDTO.getEmail() == null || requestDTO.getEmail().isBlank()) {
            requestDTO.setEmail(requestDTO.getUserId() + "@ev-mis.go.kr");
        }

        requestDTO.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        requestDTO.setLoginType("LOCAL");
        requestDTO.setStatus(defaultString(requestDTO.getStatus(), "ACTIVE"));
        requestDTO.setNickname(requestDTO.getMemberName());

        evAdminEmployeeDAO.insertMember(requestDTO);
        log.info("@# insertMember memberId => {}", requestDTO.getMemberId());

        evAdminEmployeeDAO.insertEmployee(requestDTO);
        log.info("@# insertEmployee employeeId => {}", requestDTO.getEmployeeId());

        return evAdminEmployeeDAO.findEmployeeById(requestDTO.getEmployeeId());
    }

    @Override
    @Transactional
    public EvAdminEmployeeDTO updateEmployee(Long employeeId,
                                             EvAdminEmployeeDTO requestDTO,
                                             String actorRole) {
        log.info("@# EvAdminEmployeeServiceImpl.updateEmployee()");
        log.info("@# employeeId => {}, actorRole => {}, requestDTO => {}", employeeId, actorRole, requestDTO);

        EvAdminEmployeeDTO current = evAdminEmployeeDAO.findEmployeeById(employeeId);

        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다.");
        }

        String nextRole = defaultString(requestDTO.getUserType(), current.getUserType());
        validateManagePermission(actorRole, nextRole);
        validateDuplicate(requestDTO, employeeId);

        requestDTO.setEmployeeId(employeeId);
        requestDTO.setStatus(defaultString(requestDTO.getStatus(), current.getStatus()));

        evAdminEmployeeDAO.updateMemberByEmployeeId(requestDTO);
        evAdminEmployeeDAO.updateEmployee(requestDTO);

        return evAdminEmployeeDAO.findEmployeeById(employeeId);
    }

    @Override
    @Transactional
    public void updateEmployeeStatus(Long employeeId, String status, String actorRole) {
        log.info("@# EvAdminEmployeeServiceImpl.updateEmployeeStatus()");
        log.info("@# employeeId => {}, status => {}, actorRole => {}", employeeId, status, actorRole);

        EvAdminEmployeeDTO current = evAdminEmployeeDAO.findEmployeeById(employeeId);

        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다.");
        }

        validateManagePermission(actorRole, current.getUserType());

        String nextStatus = defaultString(status, "ACTIVE");
        evAdminEmployeeDAO.updateStatus(employeeId, nextStatus);
        evAdminEmployeeDAO.updateMemberStatusByEmployeeId(employeeId, mapMemberStatus(nextStatus));
    }

    private void validateRequiredForCreate(EvAdminEmployeeDTO requestDTO) {
        if (requestDTO.getUserId() == null || requestDTO.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디는 필수입니다.");
        }

        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "초기 비밀번호는 필수입니다.");
        }

        if (requestDTO.getMemberName() == null || requestDTO.getMemberName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "직원명은 필수입니다.");
        }

        if (requestDTO.getEmployeeNo() == null || requestDTO.getEmployeeNo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사번은 필수입니다.");
        }

        if (requestDTO.getDepartmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "부서를 선택해 주세요.");
        }

        if (requestDTO.getUserType() == null || requestDTO.getUserType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "권한을 선택해 주세요.");
        }
    }

    private void validateManagePermission(String actorRole, String targetRole) {
        log.info("@# validateManagePermission actorRole => {}, targetRole => {}", actorRole, targetRole);

        if ("ADMIN".equals(actorRole)) {
            return;
        }

        if ("MANAGER".equals(actorRole)
                && ("OPERATOR".equals(targetRole) || "ENGINEER".equals(targetRole))) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "직원 권한을 관리할 수 있는 권한이 없습니다.");
    }

    private void validateDuplicate(EvAdminEmployeeDTO requestDTO, Long excludeEmployeeId) {
        if (requestDTO.getUserId() != null
                && evAdminEmployeeDAO.existsByUserId(requestDTO.getUserId(), excludeEmployeeId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        if (requestDTO.getEmail() != null
                && !requestDTO.getEmail().isBlank()
                && evAdminEmployeeDAO.existsByEmail(requestDTO.getEmail(), excludeEmployeeId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        if (requestDTO.getEmployeeNo() != null
                && evAdminEmployeeDAO.existsByEmployeeNo(requestDTO.getEmployeeNo(), excludeEmployeeId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 사번입니다.");
        }
    }

    private String mapMemberStatus(String employeeStatus) {
        if ("ACTIVE".equals(employeeStatus)) {
            return "ACTIVE";
        }

        return "INACTIVE";
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}
