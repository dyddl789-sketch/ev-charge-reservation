import { useState } from "react";

const initialFaults = [
  { id: 501, title: "커넥터 파손", station: "부산시청 공공충전소", charger: "급속 01", severity: "HIGH", status: "점검중", inspector: "박시설", result: "조치필요" },
  { id: 502, title: "통신 모듈 오류", station: "해운대 공영주차장", charger: "초급속 02", severity: "CRITICAL", status: "결재대기", inspector: "박시설", result: "교체필요" },
  { id: 503, title: "결제 단말기 오류", station: "센텀시티 공영주차장", charger: "완속 01", severity: "NORMAL", status: "접수", inspector: "미배정", result: "진행중" },
];

const FaultPage = () => {
  console.log("FaultPage 렌더링");

  const [faults, setFaults] = useState(initialFaults);

  const changeStatus = (faultId, status) => {
    console.log("장애·점검 상태 변경", faultId, status);
    setFaults((prev) => prev.map((fault) => fault.id === faultId ? { ...fault, status } : fault));
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>장애·점검관리</p>
          <h1>장애 등록·점검·조치 관리</h1>
          <span>시간을 줄이기 위해 장애관리와 점검관리를 하나의 화면에서 처리합니다.</span>
        </div>
        <button type="button" onClick={() => console.log("장애 직접 등록 클릭")}>장애 등록</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>접수 장애</span><strong>3건</strong><p>시설장애 민원 포함</p></article>
        <article className="admin-kpi-card"><span>점검중</span><strong>1건</strong><p>담당자 점검 진행</p></article>
        <article className="admin-kpi-card"><span>결재대기</span><strong>1건</strong><p>교체/비용 발생</p></article>
        <article className="admin-kpi-card"><span>긴급 장애</span><strong>1건</strong><p>CRITICAL 등급</p></article>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <div>
            <strong>장애·점검 처리 흐름</strong>
            <p>민원관리에서 시설 문제로 판단되면 이 화면에서 장애 등록, 점검, 조치, 결재 상신까지 이어집니다.</p>
          </div>
        </div>
        <div className="admin-flow-row">
          {["장애 접수", "시설담당자 배정", "점검 수행", "조치 판단", "전자결재", "조치 완료"].map((step, index) => (
            <div className="admin-flow-step" key={step}><span>{index + 1}</span><b>{step}</b></div>
          ))}
        </div>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>장애·점검 목록</strong>
          <div className="admin-filter-row compact">
            <select onChange={(e) => console.log("장애 상태 필터", e.target.value)}>
              <option>전체 상태</option>
              <option>접수</option>
              <option>점검중</option>
              <option>조치중</option>
              <option>결재대기</option>
              <option>완료</option>
            </select>
            <button type="button" onClick={() => console.log("장애 조회 클릭")}>조회</button>
          </div>
        </div>

        <div className="admin-card-list">
          {faults.map((fault) => (
            <article className="admin-work-card" key={fault.id}>
              <div className="admin-work-main">
                <div>
                  <em className={`admin-badge ${fault.severity === "CRITICAL" ? "danger" : fault.severity === "HIGH" ? "warning" : "blue"}`}>{fault.severity}</em>
                  <h3>{fault.title}</h3>
                  <p>{fault.station} · {fault.charger}</p>
                </div>
                <em className="admin-badge purple">{fault.status}</em>
              </div>

              <div className="admin-work-meta">
                <span>점검자 <b>{fault.inspector}</b></span>
                <span>점검결과 <b>{fault.result}</b></span>
                <span>장애번호 <b>#{fault.id}</b></span>
              </div>

              <div className="admin-action-row right">
                <button type="button" onClick={() => changeStatus(fault.id, "점검중")}>점검시작</button>
                <button type="button" onClick={() => changeStatus(fault.id, "조치중")}>조치중</button>
                <button type="button" onClick={() => changeStatus(fault.id, "완료")}>완료</button>
                <button type="button" className="danger" onClick={() => changeStatus(fault.id, "결재대기")}>결재상신</button>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
};

export default FaultPage;
