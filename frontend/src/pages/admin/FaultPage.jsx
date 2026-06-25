import { useEffect, useMemo, useState } from "react";
import * as adminApi from "../../apis/adminApi";

const statusOptions = ["", "접수", "점검중", "조치중", "결재대기", "완료", "취소"];
const severityOptions = ["", "LOW", "NORMAL", "HIGH", "CRITICAL"];

const getBadgeClass = (value) => {
  if (["CRITICAL", "고장", "반려"].includes(value)) {
    return "danger";
  }

  if (["HIGH", "결재대기", "조치중", "점검중"].includes(value)) {
    return "warning";
  }

  if (["완료", "정상", "사용가능"].includes(value)) {
    return "green";
  }

  if (["접수", "LOW", "NORMAL"].includes(value)) {
    return "blue";
  }

  return "purple";
};

const ProgressModal = ({ title, description, progress, children, onClose }) => {
  console.log("ProgressModal 렌더링", title, progress);

  return (
    <div className="admin-modal-backdrop">
      <div className="admin-modal fault-progress-modal">
        <div className="admin-modal-head">
          <div>
            <p>장애·점검 처리</p>
            <h2>{title}</h2>
          </div>
          <button type="button" onClick={onClose}>닫기</button>
        </div>

        <div className="fault-repair-visual">
          <div className="fault-repair-icon" aria-hidden="true">🛠️</div>
          <div>
            <strong>뚝딱뚝딱 점검 작업 진행 중</strong>
            <p>{description}</p>
          </div>
        </div>

        <div className="fault-progress-wrap">
          <div className="fault-progress-top">
            <span>진행률</span>
            <b>{progress}%</b>
          </div>
          <div className="fault-progress-bar">
            <span style={{ width: `${progress}%` }} />
          </div>
        </div>

        {children}
      </div>
    </div>
  );
};

const AssignModal = ({ fault, engineers, onClose, onSubmit }) => {
  console.log("AssignModal 렌더링", fault?.faultId);

  const [employeeId, setEmployeeId] = useState(fault?.assignedEmployeeId || "");
  const [memo, setMemo] = useState("시설관리담당자를 배정합니다.");

  const submitAssign = () => {
    console.log("장애 담당자 배정 모달 저장", fault?.faultId, employeeId);

    if (!employeeId) {
      alert("시설관리담당자를 선택해 주세요.");
      return;
    }

    onSubmit({ employeeId: Number(employeeId), memo });
  };

  return (
    <div className="admin-modal-backdrop">
      <div className="admin-modal">
        <div className="admin-modal-head">
          <div>
            <p>담당자 배정</p>
            <h2>{fault?.title}</h2>
          </div>
          <button type="button" onClick={onClose}>닫기</button>
        </div>

        <div className="admin-form-grid single">
          <label>
            시설관리담당자
            <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)}>
              <option value="">담당자 선택</option>
              {engineers.map((engineer) => (
                <option value={engineer.employeeId} key={engineer.employeeId}>
                  {engineer.memberName} / {engineer.departmentName} / {engineer.employeeNo}
                </option>
              ))}
            </select>
          </label>
          <label>
            배정 메모
            <textarea value={memo} onChange={(e) => setMemo(e.target.value)} />
          </label>
        </div>

        {engineers.length === 0 && (
          <p className="admin-help-text">ENGINEER 권한의 활성 직원이 없습니다. 인사관리에서 시설관리담당자를 먼저 등록해 주세요.</p>
        )}

        <div className="admin-action-row right">
          <button type="button" onClick={submitAssign}>배정 저장</button>
        </div>
      </div>
    </div>
  );
};


const ApprovalSubmitModal = ({ fault, onClose, onSubmit }) => {
  console.log("ApprovalSubmitModal 렌더링", fault?.faultId);

  const defaultTitle = `${fault?.stationName || "충전소"} ${fault?.chargerName || "충전기"} 교체 요청`;
  const defaultContent = `${fault?.stationName || "-"} ${fault?.chargerName || "-"} 점검 결과 교체가 필요한 상태로 확인되었습니다.\n\n장애 내용: ${fault?.description || "-"}\n점검 내용: ${fault?.latestInspectionDescription || "-"}\n\n정상 운영을 위해 부품 교체 또는 장비 교체 승인을 요청합니다.`;

  const [form, setForm] = useState({
    documentType: "충전기 교체 요청서",
    title: defaultTitle,
    content: defaultContent,
    estimatedCost: 180000,
  });

  const changeForm = (e) => {
    const { name, value } = e.target;
    console.log("전자결재 상신 입력 변경", name, value);
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const submit = () => {
    console.log("전자결재 상신 모달 저장", fault?.faultId, form);

    if (!form.title.trim()) {
      alert("문서 제목을 입력해 주세요.");
      return;
    }

    if (!form.content.trim()) {
      alert("요청 내용을 입력해 주세요.");
      return;
    }

    onSubmit({
      ...form,
      estimatedCost: Number(form.estimatedCost || 0),
    });
  };

  return (
    <div className="admin-modal-backdrop">
      <div className="admin-modal approval-submit-modal">
        <div className="admin-modal-head">
          <div>
            <p>전자결재 상신</p>
            <h2>{fault?.title}</h2>
          </div>
          <button type="button" onClick={onClose}>닫기</button>
        </div>

        <div className="approval-document-preview">
          <div className="approval-document-head">
            <span>충전기 교체 요청서</span>
            <strong>결재선: 운영관리자 → 기관장</strong>
          </div>

          <dl>
            <div><dt>연결 장애</dt><dd>#{fault?.faultId} · {fault?.title}</dd></div>
            <div><dt>충전소</dt><dd>{fault?.stationName}</dd></div>
            <div><dt>충전기</dt><dd>{fault?.chargerName} / {fault?.connectorType}</dd></div>
            <div><dt>점검 결과</dt><dd>{fault?.latestInspectionResult || "교체필요"}</dd></div>
            <div><dt>점검 내용</dt><dd>{fault?.latestInspectionDescription || "-"}</dd></div>
          </dl>
        </div>

        <div className="admin-form-grid single">
          <label>
            문서 유형
            <select name="documentType" value={form.documentType} onChange={changeForm}>
              <option value="충전기 교체 요청서">충전기 교체 요청서</option>
              <option value="부품 구매 요청서">부품 구매 요청서</option>
              <option value="시설 보수 요청서">시설 보수 요청서</option>
            </select>
          </label>
          <label>
            제목
            <input name="title" value={form.title} onChange={changeForm} />
          </label>
          <label>
            예상 비용
            <input name="estimatedCost" type="number" min="0" step="1000" value={form.estimatedCost} onChange={changeForm} />
          </label>
          <label>
            요청 내용
            <textarea name="content" value={form.content} onChange={changeForm} rows={8} />
          </label>
        </div>

        <p className="admin-help-text">상신하면 운영관리자 1차 승인, 기관장 최종 승인 순서의 결재선이 자동 생성됩니다.</p>

        <div className="admin-action-row right">
          <button type="button" onClick={submit}>결재 상신</button>
        </div>
      </div>
    </div>
  );
};

const InspectionResultForm = ({ resultForm, setResultForm, onSubmit }) => {
  console.log("InspectionResultForm 렌더링", resultForm);

  return (
    <div className="fault-result-box">
      <h3>점검이 완료되었습니다.</h3>
      <p>현장 확인 결과를 선택해 주세요. 선택한 결과에 따라 충전기 상태와 장애 상태가 자동으로 변경됩니다.</p>

      <div className="fault-result-options">
        {[
          { value: "정상", label: "정상", desc: "장애 없음 / 사용가능 복구" },
          { value: "조치필요", label: "조치필요", desc: "수리·현장 조치 필요" },
          { value: "교체필요", label: "교체필요", desc: "전자결재 상신 필요" },
        ].map((option) => (
          <button
            type="button"
            className={resultForm.inspectionResult === option.value ? "active" : ""}
            onClick={() => setResultForm((prev) => ({ ...prev, inspectionResult: option.value }))}
            key={option.value}
          >
            <b>{option.label}</b>
            <span>{option.desc}</span>
          </button>
        ))}
      </div>

      <label className="fault-result-textarea">
        점검 내용
        <textarea
          value={resultForm.description}
          onChange={(e) => setResultForm((prev) => ({ ...prev, description: e.target.value }))}
          placeholder="예: 커넥터 접점 불량 확인, 통신 모듈 응답 지연 확인"
        />
      </label>

      <div className="admin-action-row right">
        <button type="button" onClick={onSubmit}>점검 결과 저장</button>
      </div>
    </div>
  );
};

const FaultPage = () => {
  console.log("FaultPage 렌더링");

  const [faults, setFaults] = useState([]);
  const [engineers, setEngineers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [filters, setFilters] = useState({ status: "", severity: "", keyword: "" });
  const [assignFault, setAssignFault] = useState(null);
  const [inspectionModal, setInspectionModal] = useState(null);
  const [actionModal, setActionModal] = useState(null);
  const [approvalFault, setApprovalFault] = useState(null);
  const [resultForm, setResultForm] = useState({ inspectionResult: "정상", description: "" });
  const [actionResult, setActionResult] = useState("현장 조치 및 충전기 동작 확인을 완료했습니다.");

  const kpi = useMemo(() => {
    const countByStatus = (status) => faults.filter((fault) => fault.status === status).length;

    return {
      received: countByStatus("접수"),
      inspecting: countByStatus("점검중"),
      action: countByStatus("조치중"),
      approval: countByStatus("결재대기"),
      critical: faults.filter((fault) => fault.severity === "CRITICAL").length,
    };
  }, [faults]);

  const loadFaults = async (customFilters = filters) => {
    console.log("장애·점검 목록 조회", customFilters);
    setLoading(true);
    setMessage("");

    try {
      const response = await adminApi.faults(customFilters);
      console.log("장애·점검 목록 응답", response.data);
      setFaults(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.log("장애·점검 목록 조회 실패", error);
      setMessage(error.response?.data?.message || "장애·점검 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const loadEngineers = async () => {
    console.log("시설관리담당자 목록 조회");

    try {
      const response = await adminApi.faultEngineers();
      console.log("시설관리담당자 목록 응답", response.data);
      setEngineers(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.log("시설관리담당자 목록 조회 실패", error);
      setEngineers([]);
    }
  };

  useEffect(() => {
    loadFaults();
    loadEngineers();
  }, []);

  useEffect(() => {
    if (!inspectionModal || inspectionModal.phase !== "progress") {
      return undefined;
    }

    console.log("점검 진행 게이지 시작", inspectionModal.fault?.faultId);

    const timer = setInterval(() => {
      setInspectionModal((prev) => {
        if (!prev) {
          return prev;
        }

        const nextProgress = Math.min(prev.progress + 10, 100);
        if (nextProgress >= 100) {
          return { ...prev, progress: 100, phase: "result" };
        }

        return { ...prev, progress: nextProgress };
      });
    }, 500);

    return () => clearInterval(timer);
  }, [inspectionModal?.phase, inspectionModal?.fault?.faultId]);

  useEffect(() => {
    if (!actionModal || actionModal.phase !== "progress") {
      return undefined;
    }

    console.log("조치 진행 게이지 시작", actionModal.fault?.faultId);

    const timer = setInterval(() => {
      setActionModal((prev) => {
        if (!prev) {
          return prev;
        }

        const nextProgress = Math.min(prev.progress + 10, 100);
        if (nextProgress >= 100) {
          return { ...prev, progress: 100, phase: "ready" };
        }

        return { ...prev, progress: nextProgress };
      });
    }, 500);

    return () => clearInterval(timer);
  }, [actionModal?.phase, actionModal?.fault?.faultId]);

  const changeFilter = (e) => {
    const { name, value } = e.target;
    console.log("장애 필터 변경", name, value);
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const runFaultSimulation = async () => {
    console.log("장애 시뮬레이션 등록 클릭");

    try {
      const response = await adminApi.triggerFaultSimulation();
      console.log("장애 시뮬레이션 등록 응답", response.data);
      alert(response.data?.message || "장애 시뮬레이션이 등록되었습니다.");
      loadFaults();
    } catch (error) {
      console.log("장애 시뮬레이션 등록 실패", error);
      alert(error.response?.data?.message || "장애 시뮬레이션 등록에 실패했습니다.");
    }
  };

  const submitAssign = async (assignData) => {
    console.log("장애 담당자 배정 저장", assignFault?.faultId, assignData);

    try {
      await adminApi.assignFault(assignFault.faultId, assignData);
      setAssignFault(null);
      loadFaults();
    } catch (error) {
      console.log("장애 담당자 배정 실패", error);
      alert(error.response?.data?.message || "담당자 배정에 실패했습니다.");
    }
  };

  const startInspection = async (fault) => {
    console.log("점검 시작 클릭", fault.faultId);

    try {
      const response = await adminApi.startFaultInspection(fault.faultId);
      console.log("점검 시작 응답", response.data);
      const updatedFault = response.data;

      setResultForm({
        inspectionResult: "정상",
        description: "충전기 전원, 커넥터, 통신 상태를 점검했습니다.",
      });
      setInspectionModal({ fault: updatedFault, progress: 0, phase: "progress" });
      loadFaults();
    } catch (error) {
      console.log("점검 시작 실패", error);
      alert(error.response?.data?.message || "점검을 시작하지 못했습니다.");
    }
  };

  const submitInspectionResult = async () => {
    console.log("점검 결과 저장 클릭", inspectionModal?.fault?.faultId, resultForm);

    if (!inspectionModal?.fault?.faultId) {
      return;
    }

    if (!resultForm.inspectionResult) {
      alert("점검 결과를 선택해 주세요.");
      return;
    }

    try {
      const response = await adminApi.saveFaultInspectionResult(inspectionModal.fault.faultId, resultForm);
      console.log("점검 결과 저장 응답", response.data);
      const updatedFault = response.data;

      setInspectionModal(null);
      loadFaults();

      if (resultForm.inspectionResult === "조치필요") {
        setActionResult("커넥터 접점 정리, 통신 상태 확인, 충전 테스트를 완료했습니다.");
        setActionModal({ fault: updatedFault, progress: 0, phase: "progress" });
        return;
      }

      if (resultForm.inspectionResult === "교체필요") {
        alert("점검 결과 교체필요로 등록되었습니다. 전자결재 상신을 진행해 주세요.");
        return;
      }

      alert("점검 결과 정상으로 등록되어 장애가 완료 처리되었습니다.");
    } catch (error) {
      console.log("점검 결과 저장 실패", error);
      alert(error.response?.data?.message || "점검 결과 저장에 실패했습니다.");
    }
  };

  const completeAction = async () => {
    console.log("조치 완료 클릭", actionModal?.fault?.faultId, actionResult);

    if (!actionModal?.fault?.faultId) {
      return;
    }

    try {
      const response = await adminApi.completeFaultAction(actionModal.fault.faultId, { actionResult });
      console.log("조치 완료 응답", response.data);
      setActionModal(null);
      loadFaults();
      alert("조치가 완료되어 충전기가 사용가능 상태로 복구되었습니다.");
    } catch (error) {
      console.log("조치 완료 실패", error);
      alert(error.response?.data?.message || "조치 완료 처리에 실패했습니다.");
    }
  };

  const submitApproval = async (approvalData) => {
    console.log("전자결재 상신 저장", approvalFault?.faultId, approvalData);

    if (!approvalFault?.faultId) {
      return;
    }

    try {
      const response = await adminApi.submitFaultApproval(approvalFault.faultId, approvalData);
      console.log("전자결재 상신 응답", response.data);
      setApprovalFault(null);
      loadFaults();
      alert("전자결재 문서가 상신되었습니다. 전자결재 화면에서 진행 상태를 확인할 수 있습니다.");
    } catch (error) {
      console.log("전자결재 상신 실패", error);
      alert(error.response?.data?.message || "전자결재 상신에 실패했습니다.");
    }
  };

  const canStartInspection = (fault) => Boolean(fault.assignedEmployeeId) && !["완료", "취소", "결재대기"].includes(fault.status);
  const canCompleteAction = (fault) => fault.status === "조치중";

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>장애·점검관리</p>
          <h1>장애 등록·점검·조치 관리</h1>
          <span>장애 접수부터 담당자 배정, 점검 게이지, 조치 완료까지 한 화면에서 처리합니다.</span>
        </div>
        <button type="button" onClick={runFaultSimulation}>장애 시뮬레이션 등록</button>
      </div>

      <div className="admin-kpi-grid five fault-kpi-grid">
        <article className="admin-kpi-card"><span>접수 장애</span><strong>{kpi.received}건</strong><p>담당자 배정 대기</p></article>
        <article className="admin-kpi-card"><span>점검중</span><strong>{kpi.inspecting}건</strong><p>게이지 점검 가능</p></article>
        <article className="admin-kpi-card"><span>조치중</span><strong>{kpi.action}건</strong><p>수리 게이지 진행</p></article>
        <article className="admin-kpi-card"><span>결재대기</span><strong>{kpi.approval}건</strong><p>교체필요 문서 상신</p></article>
        <article className="admin-kpi-card"><span>긴급 장애</span><strong>{kpi.critical}건</strong><p>CRITICAL 등급</p></article>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <div>
            <strong>장애·점검 처리 흐름</strong>
            <p>점검 결과가 정상이면 즉시 완료, 조치필요면 수리 게이지 진행, 교체필요면 전자결재 단계로 넘어갑니다.</p>
          </div>
        </div>
        <div className="admin-flow-row fault-flow-row">
          {["장애 접수", "담당자 배정", "점검 게이지", "결과 판단", "조치/결재", "완료"].map((step, index) => (
            <div className="admin-flow-step" key={step}><span>{index + 1}</span><b>{step}</b></div>
          ))}
        </div>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>장애·점검 목록</strong>
          <div className="admin-filter-row compact fault-filter-row">
            <select name="status" value={filters.status} onChange={changeFilter}>
              {statusOptions.map((status) => <option value={status} key={status || "all"}>{status || "전체 상태"}</option>)}
            </select>
            <select name="severity" value={filters.severity} onChange={changeFilter}>
              {severityOptions.map((severity) => <option value={severity} key={severity || "all"}>{severity || "전체 등급"}</option>)}
            </select>
            <input name="keyword" value={filters.keyword} onChange={changeFilter} placeholder="충전소, 충전기, 장애명 검색" />
            <button type="button" onClick={() => loadFaults(filters)}>조회</button>
          </div>
        </div>

        {message && <div className="admin-empty-box warning">{message}</div>}
        {loading && <div className="admin-empty-box">장애·점검 목록을 불러오는 중입니다.</div>}

        {!loading && faults.length === 0 && (
          <div className="admin-empty-box">
            등록된 장애가 없습니다. 대시보드 또는 이 화면의 장애 시뮬레이션 등록 버튼으로 테스트 장애를 만들 수 있습니다.
          </div>
        )}

        <div className="admin-card-list">
          {faults.map((fault) => (
            <article className="admin-work-card fault-work-card" key={fault.faultId}>
              <div className="admin-work-main">
                <div>
                  <div className="fault-badge-row">
                    <em className={`admin-badge ${getBadgeClass(fault.severity)}`}>{fault.severity}</em>
                    <em className={`admin-badge ${getBadgeClass(fault.sourceType)}`}>{fault.sourceType}</em>
                    <em className={`admin-badge ${getBadgeClass(fault.chargerStatus)}`}>충전기 {fault.chargerStatus}</em>
                  </div>
                  <h3>{fault.title}</h3>
                  <p>{fault.stationName} · {fault.chargerName} · {fault.connectorType}</p>
                </div>
                <em className={`admin-badge ${getBadgeClass(fault.status)}`}>{fault.status}</em>
              </div>

              <div className="admin-work-meta fault-work-meta">
                <span>점검자 <b>{fault.assignedEmployeeName || "미배정"}</b></span>
                <span>점검결과 <b>{fault.latestInspectionResult || "미진행"}</b></span>
                <span>조치상태 <b>{fault.latestActionStatus || "-"}</b></span>
                <span>장애번호 <b>#{fault.faultId}</b></span>
                {fault.approvalDocumentId && <span>결재문서 <b>APR-{String(fault.approvalDocumentId).padStart(6, "0")}</b></span>}
              </div>

              <div className="fault-card-description">
                <b>장애 내용</b>
                <p>{fault.description || "등록된 장애 설명이 없습니다."}</p>
              </div>

              {fault.historyList?.length > 0 && (
                <div className="fault-history-mini">
                  {fault.historyList.slice(-3).map((history) => (
                    <span key={history.historyId}>{history.actionType} · {history.afterStatus} · {history.createdAtText}</span>
                  ))}
                </div>
              )}

              <div className="admin-action-row right">
                <button type="button" onClick={() => setAssignFault(fault)}>담당자 배정</button>
                <button type="button" disabled={!canStartInspection(fault)} onClick={() => startInspection(fault)}>점검 시작</button>
                <button type="button" disabled={!canCompleteAction(fault)} onClick={() => setActionModal({ fault, progress: 0, phase: "progress" })}>조치 진행</button>
                <button
                  type="button"
                  className="danger"
                  disabled={fault.status !== "결재대기" || Boolean(fault.approvalDocumentId)}
                  onClick={() => setApprovalFault(fault)}
                >
                  {fault.approvalDocumentId ? `상신완료(${fault.approvalStatus})` : "전자결재 상신"}
                </button>
              </div>
            </article>
          ))}
        </div>
      </div>

      {assignFault && (
        <AssignModal
          fault={assignFault}
          engineers={engineers}
          onClose={() => setAssignFault(null)}
          onSubmit={submitAssign}
        />
      )}

      {approvalFault && (
        <ApprovalSubmitModal
          fault={approvalFault}
          onClose={() => setApprovalFault(null)}
          onSubmit={submitApproval}
        />
      )}

      {inspectionModal && (
        <ProgressModal
          title={inspectionModal.phase === "progress" ? "점검 진행 중" : "점검 결과 등록"}
          description="전원 상태, 커넥터 상태, 통신 상태를 확인하고 있습니다."
          progress={inspectionModal.progress}
          onClose={() => setInspectionModal(null)}
        >
          {inspectionModal.phase === "result" && (
            <InspectionResultForm
              resultForm={resultForm}
              setResultForm={setResultForm}
              onSubmit={submitInspectionResult}
            />
          )}
        </ProgressModal>
      )}

      {actionModal && (
        <ProgressModal
          title={actionModal.phase === "progress" ? "조치 진행 중" : "조치 완료 확인"}
          description="시설관리담당자가 수리 작업을 진행하고 충전 테스트를 확인하고 있습니다."
          progress={actionModal.progress}
          onClose={() => setActionModal(null)}
        >
          {actionModal.phase === "ready" && (
            <div className="fault-result-box">
              <h3>조치 작업이 완료되었습니다.</h3>
              <p>조치 완료 내용을 저장하면 장애가 완료되고 충전기가 사용가능 상태로 복구됩니다.</p>
              <label className="fault-result-textarea">
                조치 완료 내용
                <textarea value={actionResult} onChange={(e) => setActionResult(e.target.value)} />
              </label>
              <div className="admin-action-row right">
                <button type="button" onClick={completeAction}>조치 완료 처리</button>
              </div>
            </div>
          )}
        </ProgressModal>
      )}
    </section>
  );
};

export default FaultPage;
