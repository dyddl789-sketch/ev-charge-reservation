import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as complaintApi from "../../apis/complaintApi";

const MyComplaintDetailPage = () => {
  console.log("MyComplaintDetailPage 렌더링");

  const { complaintId } = useParams();
  const [complaint, setComplaint] = useState(null);

  const loadComplaint = async () => {
    console.log("내 민원 상세 조회 실행", complaintId);

    try {
      const response = await complaintApi.getMyComplaintDetail(complaintId);
      console.log("내 민원 상세 응답", response.data);
      setComplaint(response.data);
    } catch (error) {
      console.log("내 민원 상세 조회 실패", error);
      alert(error.response?.data?.message || "민원 상세 정보를 불러오지 못했습니다.");
      setComplaint(null);
    }
  };

  useEffect(() => {
    loadComplaint();
  }, [complaintId]);

  if (!complaint) {
    return (
      <section className="complaint-form-area">
        <div className="empty-box">민원 정보를 불러오는 중입니다.</div>
      </section>
    );
  }

  return (
    <section className="complaint-form-area">
      <div className="complaint-title-box">
        <h1>민원 상세</h1>
        <p>접수한 민원의 상세 내용과 처리 이력을 확인합니다.</p>
      </div>

      <section className="complaint-detail-card">
        <div className="detail-head">
          <div>
            <span className="type-chip">{complaint.complaintType}</span>
            <h2>{complaint.title}</h2>
          </div>
          <span className={`status-badge ${complaint.status}`}>{complaint.status}</span>
        </div>

        <div className="detail-meta">
          <span>번호: {complaint.complaintId}</span>
          <span>우선순위: {complaint.priority}</span>
          <span>접수일: {complaint.createdAt}</span>
        </div>

        <div className="detail-meta">
          <span>충전소: {complaint.stationName || '-'}</span>
          <span>충전기: {complaint.chargerName || '-'}</span>
          <span>담당자: {complaint.assignedEmployeeName || '미배정'}</span>
        </div>

        <div className="detail-content">
          <h3>민원 내용</h3>
          <p>{complaint.content}</p>
        </div>

        <div className="detail-content result">
          <h3>처리 결과</h3>
          <p>{complaint.answerContent || complaint.adminMemo || '아직 처리 결과가 등록되지 않았습니다.'}</p>
          {complaint.linkedFaultId && (
            <p className="linked-fault-text">연결 장애번호 #{complaint.linkedFaultId} · 현재 상태 {complaint.linkedFaultStatus || '-'}</p>
          )}
        </div>

        <div className="history-area">
          <h3>처리 이력</h3>
          {(complaint.histories || complaint.historyList || []).length === 0 && <div className="empty-box">아직 처리 이력이 없습니다.</div>}
          {(complaint.histories || complaint.historyList || []).map((history) => (
            <div className="history-item" key={history.historyId}>
              <strong>{history.actionType}</strong>
              <p>{history.memo}</p>
              <span>{history.createdAt}</span>
            </div>
          ))}
        </div>

        <div className="complaint-btn-area">
          <Link to="/complaints/my" className="cancel-btn link-btn">목록으로</Link>
        </div>
      </section>
    </section>
  );
};

export default MyComplaintDetailPage;
