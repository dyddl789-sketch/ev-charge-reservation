package com.ev.service.admin;

import java.util.List;

import com.ev.dto.admin.employee.EvAdminDepartmentDTO;
import com.ev.dto.admin.employee.EvAdminEmployeeDTO;
import com.ev.dto.admin.employee.EvAdminMyProfileUpdateRequestDTO;
import com.ev.dto.admin.employee.EvAdminPasswordResetRequestDTO;

/*
 * 관리자 인사관리 Service
 */
public interface EvAdminEmployeeService {

    List<EvAdminEmployeeDTO> getEmployees(String keyword,
                                          Long departmentId,
                                          String userType,
                                          String status);

    EvAdminEmployeeDTO getEmployee(Long employeeId);

    List<EvAdminDepartmentDTO> getDepartments();

    EvAdminEmployeeDTO createEmployee(EvAdminEmployeeDTO requestDTO, String actorRole);

    EvAdminEmployeeDTO updateEmployee(Long employeeId,
                                      EvAdminEmployeeDTO requestDTO,
                                      String actorRole);

    void updateEmployeeStatus(Long employeeId, String status, String actorRole);

    EvAdminEmployeeDTO getMyProfile(Long memberId);

    EvAdminEmployeeDTO updateMyProfile(Long memberId, EvAdminMyProfileUpdateRequestDTO requestDTO);

    void resetEmployeePassword(Long employeeId,
                               EvAdminPasswordResetRequestDTO requestDTO,
                               String actorRole);
}

