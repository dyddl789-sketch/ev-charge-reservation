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
import com.ev.dto.admin.employee.EvAdminMyProfileUpdateRequestDTO;
import com.ev.dto.admin.employee.EvAdminPasswordResetRequestDTO;

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

        validateRequiredForCreate(requestDTO);
        applyDepartmentRoleMapping(requestDTO, null);
        validateManagePermission(actorRole, requestDTO.getUserType());
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

        requestDTO.setEmployeeId(employeeId);
        requestDTO.setDepartmentId(requestDTO.getDepartmentId() == null ? current.getDepartmentId() : requestDTO.getDepartmentId());
        requestDTO.setPositionName(defaultString(requestDTO.getPositionName(), current.getPositionName()));
        requestDTO.setDutyName(defaultString(requestDTO.getDutyName(), current.getDutyName()));
        requestDTO.setMemberName(defaultString(requestDTO.getMemberName(), current.getMemberName()));
        requestDTO.setEmail(defaultString(requestDTO.getEmail(), current.getEmail()));
        requestDTO.setPhone(requestDTO.getPhone() == null ? current.getPhone() : requestDTO.getPhone());
        requestDTO.setEmployeeNo(defaultString(requestDTO.getEmployeeNo(), current.getEmployeeNo()));
        requestDTO.setHiredAt(requestDTO.getHiredAt() == null ? current.getHiredAt() : requestDTO.getHiredAt());
        requestDTO.setRetiredAt(requestDTO.getRetiredAt() == null ? current.getRetiredAt() : requestDTO.getRetiredAt());
        requestDTO.setStatus(defaultString(requestDTO.getStatus(), current.getStatus()));

        applyDepartmentRoleMapping(requestDTO, current);
        validateManagePermission(actorRole, requestDTO.getUserType());
        validateDuplicate(requestDTO, employeeId);

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


    @Override
    public EvAdminEmployeeDTO getMyProfile(Long memberId) {
        log.info("@# EvAdminEmployeeServiceImpl.getMyProfile() memberId => {}", memberId);

        EvAdminEmployeeDTO profile = evAdminEmployeeDAO.findEmployeeByMemberId(memberId);

        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "직원 정보를 찾을 수 없습니다.");
        }

        return profile;
    }

    @Override
    @Transactional
    public EvAdminEmployeeDTO updateMyProfile(Long memberId, EvAdminMyProfileUpdateRequestDTO requestDTO) {
        log.info("@# EvAdminEmployeeServiceImpl.updateMyProfile() memberId => {}", memberId);

        if (requestDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정할 정보를 입력해 주세요.");
        }

        log.info("@# updateMyProfile passwordChangeRequested => {}", hasText(requestDTO.getNewPassword()));

        EvAdminEmployeeDTO current = evAdminEmployeeDAO.findEmployeeByMemberId(memberId);

        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "직원 정보를 찾을 수 없습니다.");
        }

        String nextEmail = defaultString(requestDTO.getEmail(), current.getEmail());
        String nextPhone = requestDTO.getPhone();

        if (nextEmail != null
                && !nextEmail.equals(current.getEmail())
                && evAdminEmployeeDAO.existsByEmail(nextEmail, current.getEmployeeId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        evAdminEmployeeDAO.updateMyProfile(memberId, nextEmail, nextPhone);

        if (hasText(requestDTO.getNewPassword()) || hasText(requestDTO.getNewPasswordConfirm())) {
            validatePasswordChange(requestDTO);

            if (!"LOCAL".equals(current.getLoginType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "소셜 로그인 계정은 MIS에서 비밀번호를 변경할 수 없습니다.");
            }

            String currentPassword = evAdminEmployeeDAO.findPasswordByMemberId(memberId);

            if (currentPassword == null || !passwordEncoder.matches(requestDTO.getCurrentPassword(), currentPassword)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
            }

            String encodedPassword = passwordEncoder.encode(requestDTO.getNewPassword());
            evAdminEmployeeDAO.updateMemberPassword(memberId, encodedPassword);
        }

        return evAdminEmployeeDAO.findEmployeeByMemberId(memberId);
    }

    @Override
    @Transactional
    public void resetEmployeePassword(Long employeeId,
                                      EvAdminPasswordResetRequestDTO requestDTO,
                                      String actorRole) {
        log.info("@# EvAdminEmployeeServiceImpl.resetEmployeePassword()");
        log.info("@# employeeId => {}, actorRole => {}", employeeId, actorRole);

        EvAdminEmployeeDTO current = evAdminEmployeeDAO.findEmployeeById(employeeId);

        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다.");
        }

        validateManagePermission(actorRole, current.getUserType());
        validatePasswordReset(requestDTO);

        String encodedPassword = passwordEncoder.encode(requestDTO.getNewPassword());
        evAdminEmployeeDAO.updateMemberPassword(current.getMemberId(), encodedPassword);
    }


    private void applyDepartmentRoleMapping(EvAdminEmployeeDTO requestDTO, EvAdminEmployeeDTO current) {
        log.info("@# applyDepartmentRoleMapping requestDTO => {}", requestDTO);

        Long departmentId = requestDTO.getDepartmentId();

        if (departmentId == null && current != null) {
            departmentId = current.getDepartmentId();
            requestDTO.setDepartmentId(departmentId);
        }

        EvAdminDepartmentDTO department = evAdminEmployeeDAO.findDepartmentById(departmentId);

        if (department == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용 가능한 부서를 선택해 주세요.");
        }

        String departmentCode = department.getDepartmentCode();
        String positionName = defaultString(requestDTO.getPositionName(), current == null ? null : current.getPositionName());

        if (!hasText(positionName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "직책/업무역할을 선택해 주세요.");
        }

        String mappedRole = mapRoleByDepartmentAndPosition(departmentCode, positionName);

        requestDTO.setDepartmentId(department.getDepartmentId());
        requestDTO.setDepartmentName(department.getDepartmentName());
        requestDTO.setDepartmentCode(departmentCode);
        requestDTO.setPositionName(positionName);
        requestDTO.setUserType(mappedRole);

        if (!hasText(requestDTO.getDutyName())) {
            requestDTO.setDutyName(mapDefaultDutyName(mappedRole));
        }

        log.info("@# mapped departmentCode => {}, positionName => {}, userType => {}", departmentCode, positionName, mappedRole);
    }

    private String mapRoleByDepartmentAndPosition(String departmentCode, String positionName) {
        if ("OPS".equals(departmentCode)) {
            if ("운영담당자".equals(positionName)) {
                return "OPERATOR";
            }

            if ("운영관리자".equals(positionName)) {
                return "MANAGER";
            }
        }

        if ("FACILITY".equals(departmentCode) && "시설관리담당자".equals(positionName)) {
            return "ENGINEER";
        }

        if ("SYSTEM".equals(departmentCode) && "기관장".equals(positionName)) {
            return "ADMIN";
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "부서와 직책/업무역할 조합이 올바르지 않습니다.");
    }

    private String mapDefaultDutyName(String userType) {
        if ("ADMIN".equals(userType)) {
            return "권한 및 시스템 총괄";
        }

        if ("MANAGER".equals(userType)) {
            return "운영 업무 배정 및 1차 승인";
        }

        if ("ENGINEER".equals(userType)) {
            return "충전기 장애 및 점검 처리";
        }

        return "민원 및 예약 운영";
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

        if (requestDTO.getPositionName() == null || requestDTO.getPositionName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "직책/업무역할을 선택해 주세요.");
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


    private void validatePasswordReset(EvAdminPasswordResetRequestDTO requestDTO) {
        if (requestDTO == null || !hasText(requestDTO.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호는 필수입니다.");
        }

        if (!requestDTO.getNewPassword().equals(requestDTO.getNewPasswordConfirm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인값이 일치하지 않습니다.");
        }

        if (requestDTO.getNewPassword().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 4자 이상 입력해 주세요.");
        }
    }

    private void validatePasswordChange(EvAdminMyProfileUpdateRequestDTO requestDTO) {
        if (!hasText(requestDTO.getCurrentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호를 입력해 주세요.");
        }

        if (!hasText(requestDTO.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호를 입력해 주세요.");
        }

        if (!requestDTO.getNewPassword().equals(requestDTO.getNewPasswordConfirm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인값이 일치하지 않습니다.");
        }

        if (requestDTO.getNewPassword().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 4자 이상 입력해 주세요.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
