package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.employee.EvAdminEmployeeDTO;
import com.ev.dto.admin.fault.EvAdminFaultDTO;
import com.ev.dto.admin.fault.EvAdminFaultHistoryDTO;

/*
 * 관리자 장애·점검 DAO
 */
@Mapper
public interface EvAdminFaultDAO {

    List<EvAdminFaultDTO> findFaultList(@Param("status") String status,
                                        @Param("severity") String severity,
                                        @Param("keyword") String keyword);

    EvAdminFaultDTO findFaultDetail(@Param("faultId") Long faultId);

    List<EvAdminFaultHistoryDTO> findFaultHistoryList(@Param("faultId") Long faultId);

    List<EvAdminEmployeeDTO> findEngineerList();

    Long findEmployeeIdByMemberId(@Param("memberId") Long memberId);

    String findFaultStatus(@Param("faultId") Long faultId);

    Long findLatestInspectionId(@Param("faultId") Long faultId);

    Long findLatestActionId(@Param("faultId") Long faultId);

    int updateFaultAssignee(@Param("faultId") Long faultId,
                            @Param("employeeId") Long employeeId);

    int updateFaultStatus(@Param("faultId") Long faultId,
                          @Param("status") String status,
                          @Param("resolved") boolean resolved);

    int updateFaultStatusAndAssignee(@Param("faultId") Long faultId,
                                     @Param("status") String status,
                                     @Param("employeeId") Long employeeId,
                                     @Param("resolved") boolean resolved);

    int updateChargerStatusByFaultId(@Param("faultId") Long faultId,
                                     @Param("status") String status);

    int updateLinkedComplaintStatus(@Param("faultId") Long faultId,
                                    @Param("status") String status,
                                    @Param("closed") boolean closed,
                                    @Param("adminMemo") String adminMemo);

    int insertFaultHistory(@Param("faultId") Long faultId,
                           @Param("employeeId") Long employeeId,
                           @Param("beforeStatus") String beforeStatus,
                           @Param("afterStatus") String afterStatus,
                           @Param("actionType") String actionType,
                           @Param("memo") String memo);

    int insertInspection(@Param("faultId") Long faultId,
                         @Param("chargerId") Long chargerId,
                         @Param("inspectorId") Long inspectorId);

    int insertInspectionByFault(@Param("faultId") Long faultId,
                                @Param("inspectorId") Long inspectorId,
                                @Param("inspectionResult") String inspectionResult,
                                @Param("description") String description,
                                @Param("actionRequired") boolean actionRequired,
                                @Param("completed") boolean completed);

    int updateInspectionResult(@Param("inspectionId") Long inspectionId,
                               @Param("inspectionResult") String inspectionResult,
                               @Param("description") String description,
                               @Param("actionRequired") boolean actionRequired);

    int insertMaintenanceAction(@Param("faultId") Long faultId,
                                @Param("inspectionId") Long inspectionId,
                                @Param("employeeId") Long employeeId,
                                @Param("actionType") String actionType);

    int completeMaintenanceAction(@Param("actionId") Long actionId,
                                  @Param("actionResult") String actionResult);

    int completeLinkedApprovalDocumentByFaultId(@Param("faultId") Long faultId);

    int countOpenFaultByCharger(@Param("chargerId") Long chargerId);

    int insertSystemFaultFromCharger(@Param("chargerId") Long chargerId,
                                     @Param("faultType") String faultType,
                                     @Param("title") String title,
                                     @Param("description") String description,
                                     @Param("severity") String severity,
                                     @Param("sourceType") String sourceType);
}
