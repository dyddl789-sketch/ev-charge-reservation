package com.ev.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.employee.EvAdminDepartmentDTO;
import com.ev.dto.admin.employee.EvAdminEmployeeDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.admin.EvAdminEmployeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 인사관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EvAdminEmployeeController {

    private final EvAdminEmployeeService evAdminEmployeeService;

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<EvAdminEmployeeDTO> employees(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "userType", required = false) String userType,
            @RequestParam(value = "status", required = false) String status) {

        log.info("@# EvAdminEmployeeController.employees()");
        return evAdminEmployeeService.getEmployees(keyword, departmentId, userType, status);
    }

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminEmployeeDTO employee(@PathVariable("employeeId") Long employeeId) {
        log.info("@# EvAdminEmployeeController.employee() employeeId => {}", employeeId);
        return evAdminEmployeeService.getEmployee(employeeId);
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<EvAdminDepartmentDTO> departments() {
        log.info("@# EvAdminEmployeeController.departments()");
        return evAdminEmployeeService.getDepartments();
    }

    @PostMapping({"/employees", "/employees/register"})
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminEmployeeDTO createEmployee(
            @RequestBody EvAdminEmployeeDTO requestDTO,
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvAdminEmployeeController.createEmployee()");
        log.info("@# requestDTO => {}", requestDTO);

        return evAdminEmployeeService.createEmployee(requestDTO, userDetails.getUserType());
    }

    @PutMapping("/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminEmployeeDTO updateEmployee(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody EvAdminEmployeeDTO requestDTO,
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvAdminEmployeeController.updateEmployee() employeeId => {}", employeeId);
        return evAdminEmployeeService.updateEmployee(employeeId, requestDTO, userDetails.getUserType());
    }

    @PutMapping("/employees/{employeeId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Map<String, Object> updateEmployeeStatus(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody Map<String, String> requestMap,
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvAdminEmployeeController.updateEmployeeStatus() employeeId => {}, requestMap => {}",
                employeeId, requestMap);

        String status = requestMap.get("status");
        evAdminEmployeeService.updateEmployeeStatus(employeeId, status, userDetails.getUserType());

        return Map.of(
                "success", true,
                "message", "직원 상태가 변경되었습니다.",
                "employeeId", employeeId,
                "status", status
        );
    }
}
