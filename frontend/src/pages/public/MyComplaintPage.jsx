import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as complaintApi from "../../apis/complaintApi";

const MyComplaintPage = () => {
  console.log("MyComplaintPage 렌더링");

  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadMyComplaints = async () => {
    console.log("내 민원 목록 조회 실행");

    try {
      setLoading(true);
      const response = await complaintApi.getMyComplaints();
      console.log("내 민원 목록 응답", response.data);
      setComplaints(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.log("내 민원 목록 조회 실패", error);
      alert(error.response?.data?.message || "내 민원 목록을 불러오지 못했습니다.");
      setComplaints([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMyComplaints();
  }, []);

  return (
    <section className="complaint-form-area">
      <div className="complaint-title-box">
        <h1>내 민원 내역</h1>
        <p>접수한 민원의 처리 상태와 진행 상황을 확인할 수 있습니다.</p>
      </div>

      <section className="complaint-list-card">
        {loading && <div className="empty-box">민원 목록을 불러오는 중입니다.</div>}

        {!loading && complaints.length === 0 && <div className="empty-box">접수한 민원이 없습니다.</div>}

        {!loading && complaints.length > 0 && (
          <table className="complaint-table">
            <thead>
              <tr>
                <th>번호</th>
                <th>유형</th>
                <th>제목</th>
                <th>충전소</th>
                <th>충전기</th>
                <th>상태</th>
                <th>접수일</th>
                <th>상세</th>
              </tr>
            </thead>
            <tbody>
              {complaints.map((item) => (
                <tr key={item.complaintId}>
                  <td>{item.complaintId}</td>
                  <td>{item.complaintType}</td>
                  <td className="left">{item.title}</td>
                  <td>{item.stationName || '-'}</td>
                  <td>{item.chargerName || '-'}</td>
                  <td><span className={`status-badge ${item.status}`}>{item.status}</span></td>
                  <td>{item.createdAt || '-'}</td>
                  <td><Link to={`/complaints/my/${item.complaintId}`} className="link-btn">상세보기</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </section>
  );
};

export default MyComplaintPage;
