import api from "./api";

// 사용자 민원 등록
export const createComplaint = (complaintData) => {
  console.log("createComplaint 요청", complaintData);
  return api.post("/complaints", complaintData);
};

// 사용자 내 민원 목록 조회
export const getMyComplaints = () => {
  console.log("getMyComplaints 요청");
  return api.get("/complaints/my");
};

// 사용자 내 민원 상세 조회
export const getMyComplaintDetail = (complaintId) => {
  console.log("getMyComplaintDetail 요청", complaintId);
  return api.get(`/complaints/my/${complaintId}`);
};

// 관리자 민원 목록 조회
export const getAdminComplaints = (params) => {
  console.log("getAdminComplaints 요청", params);
  return api.get("/admin/complaints", { params });
};

// 관리자 민원 상세 조회
export const getAdminComplaintDetail = (complaintId) => {
  console.log("getAdminComplaintDetail 요청", complaintId);
  return api.get(`/admin/complaints/${complaintId}`);
};

// 관리자 민원 담당자 배정
export const assignComplaint = (complaintId, assignData) => {
  console.log("assignComplaint 요청", complaintId, assignData);
  return api.patch(`/admin/complaints/${complaintId}/assign`, assignData);
};

// 관리자 민원 상태 변경
export const updateComplaintStatus = (complaintId, statusData) => {
  console.log("updateComplaintStatus 요청", complaintId, statusData);
  return api.patch(`/admin/complaints/${complaintId}/status`, statusData);
};

// 관리자 민원 처리 메모 등록
export const addComplaintMemo = (complaintId, memoData) => {
  console.log("addComplaintMemo 요청", complaintId, memoData);
  return api.post(`/admin/complaints/${complaintId}/histories`, memoData);
};
