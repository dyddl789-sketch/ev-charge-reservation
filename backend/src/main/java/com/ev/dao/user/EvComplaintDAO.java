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

    // 관리자 민원 목록 총 개수
    int countAdminComplaintList(EvComplaintSearchDTO searchDTO);

    // 관리자 민원 요약 카운트
    java.util.Map<String, Object> countAdminComplaintSummary(EvComplaintSearchDTO searchDTO);

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

    // 민원 답변 저장 및 완료 처리
    void updateComplaintAnswer(@Param("complaintId") Long complaintId,
                               @Param("answerContent") String answerContent);

    // 민원 기반 장애 접수 전 중복 확인
    int countFaultByComplaintId(@Param("complaintId") Long complaintId);

    // 민원 기반 장애 접수
    void insertFaultFromComplaint(@Param("complaintId") Long complaintId,
                                  @Param("faultType") String faultType,
                                  @Param("title") String title,
                                  @Param("description") String description);

    // 민원에 연결된 충전기 상태 변경
    void updateChargerStatusByComplaintId(@Param("complaintId") Long complaintId,
                                          @Param("status") String status);

    // 장애 접수 후 연결 장애 번호 조회
    Long findFaultIdByComplaintId(@Param("complaintId") Long complaintId);

    // 민원 처리 이력 저장
    void insertComplaintHistory(@Param("complaintId") Long complaintId,
                                @Param("employeeId") Long employeeId,
                                @Param("beforeStatus") String beforeStatus,
                                @Param("afterStatus") String afterStatus,
                                @Param("actionType") String actionType,
                                @Param("memo") String memo);

    // 관리자 회원의 employee_id 조회
    Long findEmployeeIdByMemberId(Long memberId);

    // 관리자 회원의 권한 조회
    String findUserTypeByMemberId(Long memberId);
}
