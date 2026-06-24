import { useState } from "react";

const initialDocuments = [
  { id: "APR-2026-001", type: "충전기 교체 요청서", title: "해운대 공영주차장 통신 모듈 교체", writer: "박시설", line: "운영관리자 → 기관장", cost: 180000, status: "상신" },
  { id: "APR-2026-002", type: "부품 구매 요청서", title: "급속 충전기 커넥터 교체 부품 구매", writer: "박시설", line: "운영관리자 → 기관장", cost: 320000, status: "1차승인" },
  { id: "APR-2026-003", type: "장애 조치 보고서", title: "부산시청 공공충전소 장애 조치 보고", writer: "김운영", line: "운영관리자", cost: 0, status: "상신" },
];

const ApprovalPage = () => {
  console.log("ApprovalPage 렌더링");

  const [documents, setDocuments] = useState(initialDocuments);
  const [selected, setSelected] = useState(initialDocuments[0]);

  const updateApproval = (documentId, status) => {
    console.log("전자결재 처리", documentId, status);
    setDocuments((prev) => prev.map((doc) => doc.id === documentId ? { ...doc, status } : doc));
    setSelected((prev) => prev.id === documentId ? { ...prev, status } : prev);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>전자결재</p>
          <h1>결재 문서 목록·승인·반려</h1>
          <span>대시보드에서는 대기 건수를 확인하고, 실제 승인/반려 처리는 전자결재 화면에서 진행합니다.</span>
        </div>
        <button type="button" onClick={() => console.log("결재 문서 작성 클릭")}>문서 작성</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 문서</span><strong>{documents.length}건</strong><p>상신 문서 기준</p></article>
        <article className="admin-kpi-card"><span>결재 대기</span><strong>2건</strong><p>승인 필요</p></article>
        <article className="admin-kpi-card"><span>1차 승인</span><strong>1건</strong><p>최종 승인 대기</p></article>
        <article className="admin-kpi-card"><span>예상 비용</span><strong>500,000원</strong><p>부품/시설 비용</p></article>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>결재 문서 목록</strong>
            <select onChange={(e) => console.log("결재 상태 필터", e.target.value)}>
              <option>전체 상태</option>
              <option>상신</option>
              <option>1차승인</option>
              <option>최종승인</option>
              <option>반려</option>
            </select>
          </div>

          <div className="admin-list selectable">
            {documents.map((doc) => (
              <button
                type="button"
                className={`admin-list-row ${selected.id === doc.id ? "active" : ""}`}
                key={doc.id}
                onClick={() => {
                  console.log("결재 문서 선택", doc.id);
                  setSelected(doc);
                }}
              >
                <div>
                  <b>{doc.title}</b>
                  <span>{doc.id} · {doc.type} · 작성자 {doc.writer}</span>
                </div>
                <em className={`admin-badge ${doc.status === "반려" ? "danger" : "purple"}`}>{doc.status}</em>
              </button>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>결재 상세</strong>
          </div>

          <div className="admin-detail-box">
            <h3>{selected.title}</h3>
            <p>{selected.id}</p>
            <dl>
              <div><dt>문서유형</dt><dd>{selected.type}</dd></div>
              <div><dt>작성자</dt><dd>{selected.writer}</dd></div>
              <div><dt>결재선</dt><dd>{selected.line}</dd></div>
              <div><dt>예상비용</dt><dd>{selected.cost.toLocaleString()}원</dd></div>
              <div><dt>상태</dt><dd>{selected.status}</dd></div>
            </dl>

            <div className="admin-approval-box">
              <strong>결재 의견</strong>
              <textarea placeholder="승인 또는 반려 의견을 입력하세요." onChange={(e) => console.log("결재 의견 입력", e.target.value)} />
            </div>

            <div className="admin-action-row">
              <button type="button" onClick={() => updateApproval(selected.id, "1차승인")}>1차 승인</button>
              <button type="button" onClick={() => updateApproval(selected.id, "최종승인")}>최종 승인</button>
              <button type="button" className="danger" onClick={() => updateApproval(selected.id, "반려")}>반려</button>
            </div>
          </div>
        </article>
      </div>
    </section>
  );
};

export default ApprovalPage;
