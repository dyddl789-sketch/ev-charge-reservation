package com.ev.dao.admin;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.approval.EvAdminApprovalDTO;
import com.ev.dto.admin.approval.EvAdminApprovalHistoryDTO;
import com.ev.dto.admin.approval.EvAdminApprovalLineDTO;

/*
 * 관리자 전자결재 DAO
 */
@Mapper
public interface EvAdminApprovalDAO {

    List<EvAdminApprovalDTO> findApprovalList(@Param("box") String box,
                                              @Param("status") String status,
                                              @Param("keyword") String keyword,
                                              @Param("currentEmployeeId") Long currentEmployeeId,
                                              @Param("userType") String userType);

    EvAdminApprovalDTO findApprovalDetail(@Param("documentId") Long documentId);

    List<EvAdminApprovalLineDTO> findApprovalLineList(@Param("documentId") Long documentId);

    List<EvAdminApprovalHistoryDTO> findApprovalHistoryList(@Param("documentId") Long documentId);

    Long findEmployeeIdByMemberId(@Param("memberId") Long memberId);

    Long findFirstActiveEmployeeIdByUserType(@Param("userType") String userType);

    int countOpenApprovalByFaultId(@Param("faultId") Long faultId);

    int insertApprovalDocument(@Param("document") EvAdminApprovalDTO documentDTO);

    int insertApprovalLine(@Param("documentId") Long documentId,
                           @Param("approverId") Long approverId,
                           @Param("approvalOrder") int approvalOrder);

    int insertApprovalHistory(@Param("documentId") Long documentId,
                              @Param("employeeId") Long employeeId,
                              @Param("actionType") String actionType,
                              @Param("beforeStatus") String beforeStatus,
                              @Param("afterStatus") String afterStatus,
                              @Param("comment") String comment);

    int updateApprovalLineDecision(@Param("lineId") Long lineId,
                                   @Param("status") String status,
                                   @Param("comment") String comment,
                                   @Param("signatureData") String signatureData);

    int updateApprovalDocumentStatus(@Param("documentId") Long documentId,
                                     @Param("status") String status,
                                     @Param("completed") boolean completed);

    int updateFaultStatusByDocumentId(@Param("documentId") Long documentId,
                                      @Param("status") String status,
                                      @Param("resolved") boolean resolved);

    int updateLinkedComplaintStatusByDocumentId(@Param("documentId") Long documentId,
                                                @Param("status") String status,
                                                @Param("closed") boolean closed,
                                                @Param("adminMemo") String adminMemo);

    int insertMaintenanceActionByDocument(@Param("documentId") Long documentId,
                                          @Param("employeeId") Long employeeId,
                                          @Param("actionType") String actionType,
                                          @Param("actionResult") String actionResult);

    int countProgressMaintenanceActionByDocument(@Param("documentId") Long documentId);

    int insertFaultHistoryByDocument(@Param("documentId") Long documentId,
                                     @Param("employeeId") Long employeeId,
                                     @Param("beforeStatus") String beforeStatus,
                                     @Param("afterStatus") String afterStatus,
                                     @Param("actionType") String actionType,
                                     @Param("memo") String memo);

    BigDecimal findTotalEstimatedCost(@Param("status") String status);
}
