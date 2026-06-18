import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as complaintApi from "../../apis/complaintApi";

const MyComplaintDetailPage = () => {
  console.log("MyComplaintDetailPage 렌더링");

  const { complaintId } = useParams();
  const [complaint, setComplaint] = useState(null);

  const mockComplaint = {
    complaintId,
    title: "충전기 예약 인증이 되지 않습니다.",
    complaintType: "예약문의",
    priority: "NORMAL",
    status: "처리중",
    content: "예약 후 인증코드를 입력했지만 인증이 되지 않습니다.",
    createdAt: "2026-06-17 10:20",
    histories: [
      {
        historyId: 1,
        actionType: "접수",
        memo: "민원이 접수되었습니다.",
        createdAt: "2026-06-17 10:20",
      },
      {
        historyId: 2,
        actionType: "배정",
        memo: "운영팀 담당자에게 배정되었습니다.",
        createdAt: "2026-06-17 10:40",
      },
    ],
  };

  const loadComplaint = async () => {
    console.log("내 민원 상세 조회 실행", complaintId);

    try {
      const response = await complaintApi.getMyComplaintDetail(complaintId);

      console.log("내 민원 상세 응답", response);

      setComplaint(response.data);
    } catch (error) {
      console.log("내 민원 상세 조회 실패 - mock 사용", error);

      setComplaint(mockComplaint);
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

          <span className={`status-badge ${complaint.status}`}>
            {complaint.status}
          </span>
        </div>

        <div className="detail-meta">
          <span>번호: {complaint.complaintId}</span>
          <span>우선순위: {complaint.priority}</span>
          <span>접수일: {complaint.createdAt}</span>
        </div>

        <div className="detail-content">
          <h3>민원 내용</h3>
          <p>{complaint.content}</p>
        </div>

        <div className="history-area">
          <h3>처리 이력</h3>

          {(complaint.histories || []).map((history) => (
            <div className="history-item" key={history.historyId}>
              <strong>{history.actionType}</strong>
              <p>{history.memo}</p>
              <span>{history.createdAt}</span>
            </div>
          ))}
        </div>

        <div className="complaint-btn-area">
          <Link to="/complaints/my" className="cancel-btn link-btn">
            목록으로
          </Link>
        </div>
      </section>
    </section>
  );
};

export default MyComplaintDetailPage;