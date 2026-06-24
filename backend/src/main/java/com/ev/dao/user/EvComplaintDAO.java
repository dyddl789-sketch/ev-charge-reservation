package com.ev.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.complaint.EvComplaintDTO;
import com.ev.dto.complaint.EvComplaintHistoryDTO;
import com.ev.dto.complaint.EvComplaintRequestDTO;
import com.ev.dto.complaint.EvComplaintSearchDTO;

@Mapper
public interface EvComplaintDAO {

    // 사용자 민원 등록
    void insertComplaint(EvComplaintRequestDTO requestDTO);

    // 내 민원 목록 조회
    List<EvComplaintDTO> findMyComplaintList(Long memberId);

    // 내 민원 상세 조회
    EvComplaintDTO findMyComplaintDetail(@Param("memberId") Long memberId,
                                          @Param("complaintId") Long complaintId);

    // 관리자 민원 목록 조회
    List<EvComplaintDTO> findAdminComplaintList(EvComplaintSearchDTO searchDTO);

    // 관리자 민원 상세 조회
    EvComplaintDTO findAdminComplaintDetail(Long complaintId);

    // 민원 처리 이력 조회
    List<EvComplaintHistoryDTO> findComplaintHistoryList(Long complaintId);

    // 민원 상태 변경
    void updateComplaintStatus(@Param("complaintId") Long complaintId,
                               @Param("status") String status,
                               @Param("adminMemo") String adminMemo,
                               @Param("closed") boolean closed);

    // 민원 담당자 배정
    void updateComplaintAssign(@Param("complaintId") Long complaintId,
                               @Param("assignedDepartmentId") Long assignedDepartmentId,
                               @Param("assignedEmployeeId") Long assignedEmployeeId);

    // 민원 처리 이력 저장
    void insertComplaintHistory(@Param("complaintId") Long complaintId,
                                @Param("employeeId") Long employeeId,
                                @Param("beforeStatus") String beforeStatus,
                                @Param("afterStatus") String afterStatus,
                                @Param("actionType") String actionType,
                                @Param("memo") String memo);

    // 관리자 회원의 employee_id 조회
    Long findEmployeeIdByMemberId(Long memberId);
}
