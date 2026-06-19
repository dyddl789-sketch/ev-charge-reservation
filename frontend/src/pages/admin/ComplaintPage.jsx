import { useState } from "react";

const initialComplaints = [
  { id: 101, title: "충전기 화면이 멈춰 있습니다", type: "시설장애", priority: "HIGH", status: "접수", member: "김성민", department: "운영팀", employee: "미배정" },
  { id: 102, title: "예약 시간을 변경하고 싶습니다", type: "예약문의", priority: "NORMAL", status: "처리중", member: "박회원", department: "운영팀", employee: "김운영" },
  { id: 103, title: "결제 취소가 반영되지 않았습니다", type: "결제문의", priority: "NORMAL", status: "배정", member: "이회원", department: "고객지원", employee: "김운영" },
  { id: 104, title: "커넥터가 파손되어 충전이 안 됩니다", type: "시설장애", priority: "URGENT", status: "장애이관", member: "최회원", department: "시설관리팀", employee: "박시설" },
];

const ComplaintPage = () => {
  console.log("Admin ComplaintPage 렌더링");

  const [complaints, setComplaints] = useState(initialComplaints);
  const [selected, setSelected] = useState(initialComplaints[0]);

  const updateStatus = (complaintId, status) => {
    console.log("민원 상태 변경", complaintId, status);
    setComplaints((prev) =>
      prev.map((item) => (item.id === complaintId ? { ...item, status } : item))
    );
    setSelected((prev) => prev.id === complaintId ? { ...prev, status } : prev);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>민원관리</p>
          <h1>민원 접수·배정·처리</h1>
          <span>사용자 민원을 확인하고 일반 문의는 처리 완료, 시설 문제는 장애·점검관리로 이관합니다.</span>
        </div>
        <button type="button" onClick={() => console.log("민원 엑셀 다운로드 클릭")}>엑셀 다운로드</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 민원</span><strong>{complaints.length}건</strong><p>접수 기준</p></article>
        <article className="admin-kpi-card"><span>미처리</span><strong>2건</strong><p>접수/배정 상태</p></article>
        <article className="admin-kpi-card"><span>시설장애</span><strong>2건</strong><p>장애·점검 이관 대상</p></article>
        <article className="admin-kpi-card"><span>긴급</span><strong>1건</strong><p>우선 처리 대상</p></article>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>민원 목록</strong>
            <div className="admin-filter-row compact">
              <select onChange={(e) => console.log("민원 상태 필터", e.target.value)}>
                <option>전체 상태</option>
                <option>접수</option>
                <option>배정</option>
                <option>처리중</option>
                <option>완료</option>
              </select>
              <button type="button" onClick={() => console.log("민원 조회 클릭")}>조회</button>
            </div>
          </div>

          <div className="admin-list selectable">
            {complaints.map((item) => (
              <button
                type="button"
                className={`admin-list-row ${selected.id === item.id ? "active" : ""}`}
                key={item.id}
                onClick={() => {
                  console.log("민원 선택", item.id);
                  setSelected(item);
                }}
              >
                <div>
                  <b>{item.title}</b>
                  <span>{item.type} · {item.member} · 담당 {item.employee}</span>
                </div>
                <em className={`admin-badge ${item.priority === "URGENT" ? "danger" : item.priority === "HIGH" ? "warning" : "blue"}`}>{item.status}</em>
              </button>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>처리 상세</strong>
          </div>

          <div className="admin-detail-box">
            <h3>{selected.title}</h3>
            <p>민원번호 #{selected.id}</p>
            <dl>
              <div><dt>유형</dt><dd>{selected.type}</dd></div>
              <div><dt>우선순위</dt><dd>{selected.priority}</dd></div>
              <div><dt>담당부서</dt><dd>{selected.department}</dd></div>
              <div><dt>담당자</dt><dd>{selected.employee}</dd></div>
              <div><dt>상태</dt><dd>{selected.status}</dd></div>
            </dl>

            <div className="admin-action-row">
              <button type="button" onClick={() => updateStatus(selected.id, "배정")}>담당자 배정</button>
              <button type="button" onClick={() => updateStatus(selected.id, "처리중")}>처리중</button>
              <button type="button" onClick={() => updateStatus(selected.id, "완료")}>완료</button>
              <button type="button" className="danger" onClick={() => updateStatus(selected.id, "장애이관")}>장애·점검 이관</button>
            </div>
          </div>
        </article>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>민원 처리 흐름</strong>
        </div>
        <div className="admin-flow-row">
          {["민원 접수", "AI 자동 분류", "운영팀 확인", "일반 문의 완료", "시설 문제 이관"].map((step, index) => (
            <div className="admin-flow-step" key={step}><span>{index + 1}</span><b>{step}</b></div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default ComplaintPage;
