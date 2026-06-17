import { useEffect, useState } from "react";
import * as complaintApi from "../../apis/complaintApi";
import "../../styles/admin-complaint.css";

const AdminComplaintPage = () => {
  console.log("AdminComplaintPage 렌더링");

  const [filters, setFilters] = useState({
    status: "",
    complaintType: "",
    keyword: "",
  });

  const [complaints, setComplaints] = useState([]);
  const [selected, setSelected] = useState(null);
  const [memo, setMemo] = useState("");

  const mockComplaints = [
    {
      complaintId: 101,
      title: "충전기 커넥터 파손 신고",
      complaintType: "시설장애",
      priority: "HIGH",
      status: "접수",
      memberName: "홍길동",
      assignedEmployeeName: "미배정",
      createdAt: "2026-06-17 09:20",
      content: "부산역 급속충전기 커넥터가 파손되어 충전이 어렵습니다.",
      aiSummary: "시설장애 가능성이 높으며 시설관리팀 배정이 필요합니다.",
    },
    {
      complaintId: 102,
      title: "예약 취소가 되지 않습니다.",
      complaintType: "예약문의",
      priority: "NORMAL",
      status: "처리중",
      memberName: "김민수",
      assignedEmployeeName: "운영담당자1",
      createdAt: "2026-06-17 10:10",
      content: "예약 취소 버튼을 눌러도 취소 처리가 되지 않습니다.",
      aiSummary: "예약 문의로 운영팀 확인이 필요합니다.",
    },
  ];

  const loadComplaints = async () => {
    console.log("관리자 민원 목록 조회 실행", filters);

    try {
      const response = await complaintApi.getAdminComplaints(filters);
      console.log("관리자 민원 목록 응답", response);
      setComplaints(response.data || []);
    } catch (error) {
      console.log("관리자 민원 목록 조회 실패 - mock 사용", error);
      setComplaints(mockComplaints);
      setSelected(mockComplaints[0]);
    }
  };

  useEffect(() => {
    loadComplaints();
  }, []);

  const changeFilter = (e) => {
    const { name, value } = e.target;
    console.log("민원 필터 변경", name, value);

    setFilters({
      ...filters,
      [name]: value,
    });
  };

  const selectComplaint = async (item) => {
    console.log("관리자 민원 선택", item);

    try {
      const response = await complaintApi.getAdminComplaintDetail(item.complaintId);
      setSelected(response.data);
    } catch (error) {
      console.log("관리자 민원 상세 조회 실패 - 목록 데이터 사용", error);
      setSelected(item);
    }
  };

  const updateStatus = async (status) => {
    if (!selected) return;

    console.log("민원 상태 변경", selected.complaintId, status);

    try {
      await complaintApi.updateComplaintStatus(selected.complaintId, { status });
      alert("민원 상태가 변경되었습니다.");
      setSelected({ ...selected, status });
      loadComplaints();
    } catch (error) {
      console.log("민원 상태 변경 실패", error);
      alert("현재는 화면 확인용으로 상태만 변경합니다.");
      setSelected({ ...selected, status });
    }
  };

  const assignToOperator = async () => {
    if (!selected) return;

    const assignData = {
      assignedDepartmentId: 1,
      assignedEmployeeId: 1,
    };

    console.log("민원 담당자 배정", selected.complaintId, assignData);

    try {
      await complaintApi.assignComplaint(selected.complaintId, assignData);
      alert("담당자가 배정되었습니다.");
      loadComplaints();
    } catch (error) {
      console.log("민원 담당자 배정 실패", error);
      alert("현재는 화면 확인용입니다. 백엔드 연결 후 실제 배정됩니다.");
    }
  };

  const addMemo = async () => {
    if (!selected || !memo.trim()) {
      alert("처리 메모를 입력하세요.");
      return;
    }

    console.log("민원 처리 메모 등록", selected.complaintId, memo);

    try {
      await complaintApi.addComplaintMemo(selected.complaintId, {
        actionType: "처리메모",
        memo,
      });
      alert("처리 메모가 등록되었습니다.");
      setMemo("");
    } catch (error) {
      console.log("민원 처리 메모 등록 실패", error);
      alert("현재는 화면 확인용입니다. 백엔드 연결 후 저장됩니다.");
    }
  };

  return (
    <main className="admin-complaint-page">
      <section className="admin-page-head">
        <div>
          <p>Complaint Management</p>
          <h1>민원관리</h1>
        </div>
        <button type="button" onClick={loadComplaints}>조회</button>
      </section>

      <section className="complaint-kpi-grid">
        <div className="kpi-card">
          <span>접수</span>
          <strong>{complaints.filter((item) => item.status === "접수").length}</strong>
        </div>
        <div className="kpi-card">
          <span>처리중</span>
          <strong>{complaints.filter((item) => item.status === "처리중").length}</strong>
        </div>
        <div className="kpi-card">
          <span>시설장애</span>
          <strong>{complaints.filter((item) => item.complaintType === "시설장애").length}</strong>
        </div>
        <div className="kpi-card">
          <span>긴급/높음</span>
          <strong>{complaints.filter((item) => item.priority === "HIGH" || item.priority === "URGENT").length}</strong>
        </div>
      </section>

      <section className="admin-filter-card">
        <select name="status" value={filters.status} onChange={changeFilter}>
          <option value="">전체 상태</option>
          <option value="접수">접수</option>
          <option value="확인중">확인중</option>
          <option value="배정">배정</option>
          <option value="처리중">처리중</option>
          <option value="완료">완료</option>
          <option value="반려">반려</option>
        </select>

        <select name="complaintType" value={filters.complaintType} onChange={changeFilter}>
          <option value="">전체 유형</option>
          <option value="예약문의">예약문의</option>
          <option value="결제문의">결제문의</option>
          <option value="시설장애">시설장애</option>
          <option value="회원문의">회원문의</option>
          <option value="기타">기타</option>
        </select>

        <input
          type="text"
          name="keyword"
          value={filters.keyword}
          onChange={changeFilter}
          placeholder="제목 또는 회원명 검색"
        />
      </section>

      <section className="admin-complaint-layout">
        <div className="admin-list-panel">
          <table>
            <thead>
              <tr>
                <th>번호</th>
                <th>유형</th>
                <th>제목</th>
                <th>상태</th>
                <th>담당자</th>
              </tr>
            </thead>
            <tbody>
              {complaints.map((item) => (
                <tr
                  key={item.complaintId}
                  onClick={() => selectComplaint(item)}
                  className={selected?.complaintId === item.complaintId ? "active" : ""}
                >
                  <td>{item.complaintId}</td>
                  <td>{item.complaintType}</td>
                  <td>{item.title}</td>
                  <td>{item.status}</td>
                  <td>{item.assignedEmployeeName || "미배정"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="admin-detail-panel">
          {!selected && <div className="empty-box">민원을 선택하세요.</div>}

          {selected && (
            <>
              <div className="detail-title-row">
                <div>
                  <span className="type-chip">{selected.complaintType}</span>
                  <h2>{selected.title}</h2>
                  <p>{selected.memberName} · {selected.createdAt}</p>
                </div>
                <span className={`status-badge ${selected.status}`}>{selected.status}</span>
              </div>

              <div className="ai-summary-box">
                <strong>AI 민원 분석</strong>
                <p>{selected.aiSummary || "AI 분석 결과가 없습니다."}</p>
              </div>

              <div className="content-box">
                <h3>민원 내용</h3>
                <p>{selected.content}</p>
              </div>

              <div className="button-row">
                <button type="button" onClick={() => updateStatus("확인중")}>확인중</button>
                <button type="button" onClick={assignToOperator}>담당자 배정</button>
                <button type="button" onClick={() => updateStatus("처리중")}>처리중</button>
                <button type="button" onClick={() => updateStatus("완료")}>완료</button>
              </div>

              <div className="memo-box">
                <textarea
                  value={memo}
                  onChange={(e) => setMemo(e.target.value)}
                  placeholder="처리 메모를 입력하세요."
                />
                <button type="button" onClick={addMemo}>메모 등록</button>
              </div>
            </>
          )}
        </div>
      </section>
    </main>
  );
};

export default AdminComplaintPage;
