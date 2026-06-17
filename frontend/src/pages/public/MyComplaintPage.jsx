import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as complaintApi from "../../apis/complaintApi";
import "../../styles/complaint.css";

const MyComplaintPage = () => {
  console.log("MyComplaintPage 렌더링");

  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(false);

  const mockComplaints = [
    {
      complaintId: 1,
      title: "충전기 예약 인증이 되지 않습니다.",
      complaintType: "예약문의",
      priority: "NORMAL",
      status: "접수",
      createdAt: "2026-06-17 10:20",
    },
    {
      complaintId: 2,
      title: "커넥터가 파손되어 충전이 어렵습니다.",
      complaintType: "시설장애",
      priority: "HIGH",
      status: "처리중",
      createdAt: "2026-06-16 15:40",
    },
  ];

  const loadMyComplaints = async () => {
    console.log("내 민원 목록 조회 실행");

    try {
      setLoading(true);
      const response = await complaintApi.getMyComplaints();
      console.log("내 민원 목록 응답", response);
      setComplaints(response.data || []);
    } catch (error) {
      console.log("내 민원 목록 조회 실패 - mock 사용", error);
      setComplaints(mockComplaints);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMyComplaints();
  }, []);

  return (
    <main className="complaint-page">
      <section className="complaint-hero">
        <div>
          <p className="eyebrow">My Complaints</p>
          <h1>내 민원 내역</h1>
          <p>접수한 민원의 처리 상태와 진행 상황을 확인할 수 있습니다.</p>
        </div>
        <Link to="/complaint" className="outline-link-btn">
          민원 접수
        </Link>
      </section>

      <section className="complaint-list-card">
        {loading && <div className="empty-box">민원 목록을 불러오는 중입니다.</div>}

        {!loading && complaints.length === 0 && (
          <div className="empty-box">접수한 민원이 없습니다.</div>
        )}

        {!loading && complaints.length > 0 && (
          <table className="complaint-table">
            <thead>
              <tr>
                <th>번호</th>
                <th>유형</th>
                <th>제목</th>
                <th>우선순위</th>
                <th>상태</th>
                <th>접수일</th>
              </tr>
            </thead>
            <tbody>
              {complaints.map((item) => (
                <tr key={item.complaintId}>
                  <td>{item.complaintId}</td>
                  <td>{item.complaintType}</td>
                  <td className="title-cell">
                    <Link to={`/complaints/my/${item.complaintId}`}>
                      {item.title}
                    </Link>
                  </td>
                  <td>{item.priority}</td>
                  <td>
                    <span className={`status-badge ${item.status}`}>
                      {item.status}
                    </span>
                  </td>
                  <td>{item.createdAt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </main>
  );
};

export default MyComplaintPage;
