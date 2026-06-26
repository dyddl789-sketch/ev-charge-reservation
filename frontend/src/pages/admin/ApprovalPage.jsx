import { useEffect, useMemo, useRef, useState } from "react";
import { useOutletContext, useSearchParams } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";
import { getRole } from "../../utils/adminRoleUtils";

const approvalBoxes = [
  { value: "inbox", label: "내 결재함" },
  { value: "submitted", label: "상신함" },
  { value: "progress", label: "진행중" },
  { value: "closed", label: "완료/반려" },
];

const statusOptions = ["", "상신", "1차승인", "최종승인", "반려", "완료", "취소"];

const getBadgeClass = (status) => {
  if (["반려", "취소", "고장"].includes(status)) {
    return "danger";
  }

  if (["상신", "1차승인", "대기", "결재대기"].includes(status)) {
    return "warning";
  }

  if (["최종승인", "완료", "승인", "사용가능"].includes(status)) {
    return "green";
  }

  return "purple";
};

const formatCost = (value) => {
  const numberValue = Number(value || 0);
  return `${numberValue.toLocaleString()}원`;
};

const getApprovalRoleLabel = (line) => {
  if (line?.approvalOrder === 1) {
    return "운영관리자 1차 승인";
  }

  if (line?.approvalOrder === 2) {
    return "기관장 최종 승인";
  }

  return `${line?.approvalOrder || "-"}차 승인`;
};

const getNextActionLabel = (selected) => {
  if (!selected) {
    return "";
  }

  if (selected.status === "상신") {
    return "운영관리자 1차 승인 대기";
  }

  if (selected.status === "1차승인") {
    return "기관장 최종 승인 대기";
  }

  if (selected.status === "최종승인") {
    return "최종 승인 완료 · 장애점검관리에서 교체 작업 진행";
  }

  if (selected.status === "완료") {
    return "결재 및 조치 완료";
  }

  if (selected.status === "반려") {
    return "반려 완료 · 상신자 확인 필요";
  }

  return selected.status;
};

const SignaturePad = ({ value, onChange }) => {
  const canvasRef = useRef(null);
  const drawingRef = useRef(false);
  const hasDrawnRef = useRef(Boolean(value));

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) {
      return;
    }

    const context = canvas.getContext("2d");
    context.lineWidth = 2.6;
    context.lineCap = "round";
    context.lineJoin = "round";
    context.strokeStyle = "#0f172a";

    if (value) {
      const image = new Image();
      image.onload = () => {
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.drawImage(image, 0, 0, canvas.width, canvas.height);
      };
      image.src = value;
      hasDrawnRef.current = true;
    }
  }, [value]);

  const getPoint = (event) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const clientX = event.touches?.[0]?.clientX ?? event.clientX;
    const clientY = event.touches?.[0]?.clientY ?? event.clientY;

    return {
      x: ((clientX - rect.left) / rect.width) * canvas.width,
      y: ((clientY - rect.top) / rect.height) * canvas.height,
    };
  };

  const startDrawing = (event) => {
    event.preventDefault();
    const canvas = canvasRef.current;
    const context = canvas.getContext("2d");
    const point = getPoint(event);

    drawingRef.current = true;
    context.beginPath();
    context.moveTo(point.x, point.y);
  };

  const draw = (event) => {
    if (!drawingRef.current) {
      return;
    }

    event.preventDefault();
    const canvas = canvasRef.current;
    const context = canvas.getContext("2d");
    const point = getPoint(event);

    context.lineTo(point.x, point.y);
    context.stroke();
    hasDrawnRef.current = true;
    onChange(canvas.toDataURL("image/png"));
  };

  const stopDrawing = () => {
    if (!drawingRef.current) {
      return;
    }

    drawingRef.current = false;
    const canvas = canvasRef.current;
    onChange(hasDrawnRef.current ? canvas.toDataURL("image/png") : "");
  };

  const clearSignature = () => {
    const canvas = canvasRef.current;
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    hasDrawnRef.current = false;
    onChange("");
  };

  return (
    <div className="approval-signature-wrap">
      <canvas
        ref={canvasRef}
        width="560"
        height="180"
        onMouseDown={startDrawing}
        onMouseMove={draw}
        onMouseUp={stopDrawing}
        onMouseLeave={stopDrawing}
        onTouchStart={startDrawing}
        onTouchMove={draw}
        onTouchEnd={stopDrawing}
      />
      <div className="approval-signature-actions">
        <span>마우스 또는 터치로 서명해 주세요.</span>
        <button type="button" onClick={clearSignature}>서명 지우기</button>
      </div>
    </div>
  );
};

const ApprovalDecisionModal = ({ selected, actionType, onClose, onSubmit }) => {
  const isReject = actionType === "reject";
  const [comment, setComment] = useState(isReject ? "" : "확인 후 승인합니다.");
  const [signatureData, setSignatureData] = useState("");
  const [saving, setSaving] = useState(false);

  const submit = async () => {
    console.log("전자결재 처리 모달 제출", actionType, selected?.documentId);

    if (isReject && !comment.trim()) {
      alert("반려 사유를 입력해 주세요.");
      return;
    }

    if (!isReject && !signatureData) {
      alert("승인 전자서명을 입력해 주세요.");
      return;
    }

    try {
      setSaving(true);
      await onSubmit({ comment, signatureData });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="admin-modal-backdrop">
      <div className="admin-modal approval-decision-modal">
        <div className="admin-modal-head">
          <div>
            <p>{isReject ? "전자결재 반려" : "전자결재 승인"}</p>
            <h2>{selected.title}</h2>
          </div>
          <button type="button" onClick={onClose}>닫기</button>
        </div>

        <div className="approval-decision-paper">
          <div className="approval-paper-head">
            <span>{selected.documentType}</span>
            <em className={`admin-badge ${getBadgeClass(selected.status)}`}>{selected.status}</em>
          </div>
          <h3>{selected.title}</h3>
          <p>{selected.documentNo || `APR-${String(selected.documentId).padStart(6, "0")}`} · 예상비용 {formatCost(selected.estimatedCost)}</p>
          <dl>
            <div><dt>작성자</dt><dd>{selected.writerName} / {selected.writerDepartmentName}</dd></div>
            <div><dt>연결 장애</dt><dd>#{selected.faultId} · {selected.faultTitle}</dd></div>
            <div><dt>충전소</dt><dd>{selected.stationName}</dd></div>
            <div><dt>충전기</dt><dd>{selected.chargerName} / {selected.connectorType}</dd></div>
          </dl>
          <div className="approval-content-box">
            <strong>요청 내용</strong>
            <p>{selected.content}</p>
          </div>
        </div>

        <div className="approval-decision-form">
          <label>
            {isReject ? "반려 사유" : "결재 의견"}
            <textarea value={comment} onChange={(e) => setComment(e.target.value)} placeholder={isReject ? "반려 사유를 입력해 주세요." : "결재 의견을 입력해 주세요."} />
          </label>

          <div className="approval-signature-section">
            <strong>{isReject ? "전자서명 선택" : "전자서명"}</strong>
            <p>{isReject ? "반려는 사유가 필수이며, 서명은 선택입니다." : "승인 처리를 위해 결재자 서명이 필요합니다."}</p>
            <SignaturePad value={signatureData} onChange={setSignatureData} />
          </div>
        </div>

        <div className="admin-action-row right">
          <button type="button" onClick={onClose}>취소</button>
          <button type="button" className={isReject ? "danger" : ""} onClick={submit} disabled={saving}>
            {saving ? "처리 중" : isReject ? "반려 처리" : "승인 처리"}
          </button>
        </div>
      </div>
    </div>
  );
};

const ApprovalPage = () => {
  console.log("ApprovalPage 렌더링");

  const [searchParams] = useSearchParams();
  const outletContext = useOutletContext();
  const currentRole = getRole(outletContext?.currentUser);

  const [documents, setDocuments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [decisionModal, setDecisionModal] = useState(null);
  const [filters, setFilters] = useState({
    box: "inbox",
    status: "",
    keyword: "",
  });

  const kpi = useMemo(() => {
    const countByStatus = (status) => documents.filter((doc) => doc.status === status).length;
    const totalCost = documents.reduce((sum, doc) => sum + Number(doc.estimatedCost || 0), 0);

    return {
      total: documents.length,
      submit: countByStatus("상신"),
      first: countByStatus("1차승인"),
      cost: totalCost,
    };
  }, [documents]);

  const canApproveSelected = selected && (
    (selected.status === "상신" && currentRole === "MANAGER")
    || (selected.status === "1차승인" && currentRole === "ADMIN")
  );

  const loadDetail = async (documentId) => {
    console.log("전자결재 상세 조회", documentId);

    try {
      const response = await adminApi.approvalDetail(documentId);
      console.log("전자결재 상세 응답", response.data);
      setSelected(response.data);
      return response.data;
    } catch (error) {
      console.log("전자결재 상세 조회 실패", error);
      alert(error.response?.data?.message || "전자결재 상세를 불러오지 못했습니다.");
      return null;
    }
  };

  const loadDocuments = async (customFilters = filters, keepDocumentId = selected?.documentId) => {
    console.log("전자결재 목록 조회", customFilters, keepDocumentId);
    setLoading(true);
    setMessage("");

    try {
      const response = await adminApi.approvals(customFilters);
      console.log("전자결재 목록 응답", response.data);
      const list = Array.isArray(response.data) ? response.data : [];
      setDocuments(list);

      const queryDocumentId = searchParams.get("documentId");
      const targetDocumentId = keepDocumentId || queryDocumentId;
      const firstDocument = targetDocumentId
        ? list.find((doc) => String(doc.documentId) === String(targetDocumentId)) || list[0]
        : list[0];

      if (firstDocument?.documentId) {
        loadDetail(firstDocument.documentId);
      } else {
        setSelected(null);
      }
    } catch (error) {
      console.log("전자결재 목록 조회 실패", error);
      setMessage(error.response?.data?.message || "전자결재 목록을 불러오지 못했습니다.");
      setDocuments([]);
      setSelected(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadDocuments(filters, searchParams.get("documentId"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const changeFilter = (e) => {
    const { name, value } = e.target;
    console.log("전자결재 필터 변경", name, value);
    const nextFilters = { ...filters, [name]: value };
    setFilters(nextFilters);

    if (name === "box" || name === "status") {
      loadDocuments(nextFilters, null);
    }
  };

  const selectDocument = (doc) => {
    console.log("결재 문서 선택", doc.documentId);
    loadDetail(doc.documentId);
  };

  const openDecisionModal = (actionType) => {
    console.log("전자결재 처리 모달 열기", actionType, selected?.documentId);

    if (!selected) {
      return;
    }

    setDecisionModal({ actionType });
  };

  const submitDecision = async (decisionData) => {
    console.log("전자결재 처리 요청", decisionModal?.actionType, selected?.documentId, decisionData);

    if (!selected?.documentId || !decisionModal?.actionType) {
      return;
    }

    try {
      const response = decisionModal.actionType === "reject"
        ? await adminApi.rejectApproval(selected.documentId, decisionData)
        : await adminApi.approveApproval(selected.documentId, decisionData);

      console.log("전자결재 처리 응답", response.data);
      setDecisionModal(null);
      await loadDocuments(filters, selected.documentId);
      alert(decisionModal.actionType === "reject" ? "전자결재 문서를 반려했습니다." : "전자결재 문서를 승인했습니다.");
    } catch (error) {
      console.log("전자결재 처리 실패", error);
      alert(error.response?.data?.message || "전자결재 처리에 실패했습니다.");
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>전자결재</p>
          <h1>결재 문서함</h1>
          <span>운영관리자 1차 승인, 기관장 최종 승인, 전자서명까지 한 화면에서 처리합니다.</span>
        </div>
        <button type="button" onClick={() => alert("장애점검관리에서 교체필요 장애를 선택한 뒤 전자결재를 상신해 주세요.")}>문서 작성 안내</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>문서 수</span><strong>{kpi.total}건</strong><p>현재 탭 기준</p></article>
        <article className="admin-kpi-card"><span>상신</span><strong>{kpi.submit}건</strong><p>1차 승인 대기</p></article>
        <article className="admin-kpi-card"><span>1차 승인</span><strong>{kpi.first}건</strong><p>최종 승인 대기</p></article>
        <article className="admin-kpi-card"><span>예상 비용</span><strong>{formatCost(kpi.cost)}</strong><p>현재 목록 합계</p></article>
      </div>

      <div className="admin-panel approval-tab-panel">
        <div className="approval-tabs">
          {approvalBoxes.map((box) => (
            <button
              type="button"
              className={filters.box === box.value ? "active" : ""}
              key={box.value}
              onClick={() => changeFilter({ target: { name: "box", value: box.value } })}
            >
              {box.label}
            </button>
          ))}
        </div>
        <div className="admin-filter-row compact">
          <select name="status" value={filters.status} onChange={changeFilter}>
            {statusOptions.map((status) => <option value={status} key={status || "all"}>{status || "전체 상태"}</option>)}
          </select>
          <input name="keyword" value={filters.keyword} onChange={changeFilter} placeholder="문서 제목, 충전소, 작성자 검색" />
          <button type="button" onClick={() => loadDocuments(filters)}>조회</button>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>결재 문서 목록</strong>
            <span>{loading ? "조회 중" : `${documents.length}건`}</span>
          </div>

          {message && <div className="admin-empty-box warning">{message}</div>}
          {!loading && documents.length === 0 && !message && (
            <div className="admin-empty-box">표시할 전자결재 문서가 없습니다.</div>
          )}

          <div className="admin-list selectable approval-list">
            {documents.map((doc) => (
              <button
                type="button"
                className={`admin-list-row ${selected?.documentId === doc.documentId ? "active" : ""}`}
                key={doc.documentId}
                onClick={() => selectDocument(doc)}
              >
                <div>
                  <b>{doc.title}</b>
                  <span>{doc.documentNo || `APR-${String(doc.documentId).padStart(6, "0")}`} · {doc.documentType} · 작성자 {doc.writerName}</span>
                  <small>{doc.stationName || "-"} · {doc.chargerName || "-"} · 현재 결재자 {doc.currentApproverName || "-"}</small>
                </div>
                <em className={`admin-badge ${getBadgeClass(doc.status)}`}>{doc.status}</em>
              </button>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>결재 상세</strong>
            {selected && <span>{getNextActionLabel(selected)}</span>}
          </div>

          {!selected && <div className="admin-empty-box">문서를 선택해 주세요.</div>}

          {selected && (
            <div className="admin-detail-box approval-detail-box">
              <div className="approval-document-paper">
                <div className="approval-paper-head">
                  <span>{selected.documentType}</span>
                  <em className={`admin-badge ${getBadgeClass(selected.status)}`}>{selected.status}</em>
                </div>
                <h3>{selected.title}</h3>
                <p>{selected.documentNo || `APR-${String(selected.documentId).padStart(6, "0")}`} · {selected.createdAtText}</p>

                <dl>
                  <div><dt>작성자</dt><dd>{selected.writerName} / {selected.writerDepartmentName}</dd></div>
                  <div><dt>연결 장애</dt><dd>#{selected.faultId} · {selected.faultTitle}</dd></div>
                  <div><dt>충전소</dt><dd>{selected.stationName}</dd></div>
                  <div><dt>충전기</dt><dd>{selected.chargerName} / {selected.connectorType}</dd></div>
                  <div><dt>점검결과</dt><dd>{selected.inspectionResult}</dd></div>
                  <div><dt>예상비용</dt><dd>{formatCost(selected.estimatedCost)}</dd></div>
                  <div><dt>현재 결재자</dt><dd>{selected.currentApproverName || "-"}</dd></div>
                  <div><dt>충전기 상태</dt><dd>{selected.chargerStatus || "-"}</dd></div>
                </dl>

                <div className="approval-content-box">
                  <strong>요청 내용</strong>
                  <p>{selected.content}</p>
                </div>

                <div className="approval-line-box">
                  <strong>결재선</strong>
                  <div className="approval-line-list">
                    {selected.lineList?.map((line) => (
                      <div className="approval-line-item with-sign" key={line.lineId}>
                        <div>
                          <span>{getApprovalRoleLabel(line)}</span>
                          <b>{line.approverName}</b>
                          <small>{line.departmentName || "-"} · {line.positionName || "-"}</small>
                        </div>
                        <div className="approval-line-result">
                          {line.signatureData ? (
                            <img src={line.signatureData} alt={`${line.approverName} 서명`} />
                          ) : (
                            <i>서명 대기</i>
                          )}
                          <em className={`admin-badge ${getBadgeClass(line.status)}`}>{line.status}</em>
                          <small>{line.approvedAtText}</small>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="approval-history-box">
                  <strong>처리 이력</strong>
                  {selected.historyList?.length > 0 ? (
                    selected.historyList.map((history) => (
                      <p key={history.historyId}>{history.createdAtText} · {history.employeeName} · {history.actionType} · {history.afterStatus} {history.comment ? `· ${history.comment}` : ""}</p>
                    ))
                  ) : (
                    <p>처리 이력이 없습니다.</p>
                  )}
                </div>
              </div>

              <div className="admin-action-row right">
                <button type="button" disabled={!canApproveSelected} onClick={() => openDecisionModal("approve")}>승인/서명</button>
                <button type="button" className="danger" disabled={!canApproveSelected} onClick={() => openDecisionModal("reject")}>반려</button>
              </div>

              {!canApproveSelected && ["상신", "1차승인"].includes(selected.status) && (
                <div className="admin-help-text">현재 로그인 권한({currentRole || "-"})으로 처리할 차례가 아니거나, 내 결재함 대상 문서가 아닙니다.</div>
              )}
            </div>
          )}
        </article>
      </div>

      {decisionModal && selected && (
        <ApprovalDecisionModal
          selected={selected}
          actionType={decisionModal.actionType}
          onClose={() => setDecisionModal(null)}
          onSubmit={submitDecision}
        />
      )}
    </section>
  );
};

export default ApprovalPage;
