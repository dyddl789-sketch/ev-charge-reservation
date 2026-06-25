package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.employee.EvAdminDepartmentDTO;
import com.ev.dto.admin.employee.EvAdminEmployeeDTO;

/*
 * 관리자 인사관리 DAO
 */
@Mapper
public interface EvAdminEmployeeDAO {

    List<EvAdminEmployeeDTO> findEmployees(@Param("keyword") String keyword,
                                           @Param("departmentId") Long departmentId,
                                           @Param("userType") String userType,
                                           @Param("status") String status);

    EvAdminEmployeeDTO findEmployeeById(@Param("employeeId") Long employeeId);

    List<EvAdminDepartmentDTO> findDepartments();

    EvAdminDepartmentDTO findDepartmentById(@Param("departmentId") Long departmentId);

    int insertMember(EvAdminEmployeeDTO employeeDTO);

    int insertEmployee(EvAdminEmployeeDTO employeeDTO);

    int updateMemberByEmployeeId(EvAdminEmployeeDTO employeeDTO);

    int updateEmployee(EvAdminEmployeeDTO employeeDTO);

    int updateStatus(@Param("employeeId") Long employeeId,
                     @Param("status") String status);

    int updateMemberStatusByEmployeeId(@Param("employeeId") Long employeeId,
                                       @Param("status") String status);

    int existsByUserId(@Param("userId") String userId,
                       @Param("excludeEmployeeId") Long excludeEmployeeId);

    int existsByEmail(@Param("email") String email,
                      @Param("excludeEmployeeId") Long excludeEmployeeId);

    int existsByEmployeeNo(@Param("employeeNo") String employeeNo,
                           @Param("excludeEmployeeId") Long excludeEmployeeId);


    EvAdminEmployeeDTO findEmployeeByMemberId(@Param("memberId") Long memberId);

    String findPasswordByMemberId(@Param("memberId") Long memberId);

    int updateMyProfile(@Param("memberId") Long memberId,
                        @Param("email") String email,
                        @Param("phone") String phone);

    int updateMemberPassword(@Param("memberId") Long memberId,
                             @Param("password") String password);
}
