import { useNavigate } from "react-router-dom";

const summary = [
  { label: "전체 예약", value: 330 },
  { label: "예약완료", value: 0 },
  { label: "인증완료", value: 0 },
  { label: "충전중", value: 0 },
  { label: "완료", value: 296 },
  { label: "취소", value: 6 },
  { label: "노쇼", value: 28 },
];

const issues = [
  { label: "인증 대기", value: 0 },
  { label: "취소 건수", value: 0 },
  { label: "예상 노쇼", value: 0 },
  { label: "시작 지연", value: 0 },
];

const reservations = [
  { id: "RSV-000580", time: "2026-06-05 11:37", member: "시연회원14", vehicle: "BMW i4", station: "테스트 해운대 충전소", charger: "해운대 완속 3번", soc: "48% → 85%", minutes: "306분", cost: "8,692원", code: "ADM_R_00028", status: "노쇼" },
  { id: "RSV-000629", time: "2026-06-05 11:25", member: "김성민", vehicle: "BMW i4", station: "테스트 기장 충전소", charger: "기장 급속 2번", soc: "30% → 32%", minutes: "2분", cost: "583원", code: "-", status: "노쇼" },
  { id: "RSV-000628", time: "2026-06-05 11:20", member: "김성민", vehicle: "BMW i4", station: "테스트 기장 충전소", charger: "기장 급속 2번", soc: "30% → 33%", minutes: "2분", cost: "875원", code: "-", status: "완료" },
  { id: "RSV-000579", time: "2026-06-05 10:52", member: "시연회원9", vehicle: "BMW i4", station: "테스트 해운대 충전소", charger: "해운대 급속 2번", soc: "47% → 85%", minutes: "22분", cost: "11,069원", code: "ADM_R_00027", status: "노쇼" },
];

const AdminReservationPage = () => {
  console.log("AdminReservationPage 렌더링");

  const navigate = useNavigate();

  const goDetail = (reservationId) => {
    console.log("예약 상세 이동", reservationId);
    navigate(`/admin/reservations/${reservationId}`);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>예약관리</p>
          <h1>예약 현황</h1>
          <span>전체 충전 예약 상태와 예약 이슈를 관리합니다.</span>
        </div>
      </div>

      <div className="admin-stat-strip">
        {summary.map((item) => (
          <article key={item.label}>
            <span>{item.label}</span>
            <strong>{item.value}</strong>
          </article>
        ))}
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>오늘 예약 이슈</strong>
        </div>
        <div className="admin-issue-grid">
          {issues.map((item) => (
            <article key={item.label}>
              <span>{item.label}</span>
              <strong>{item.value}</strong>
            </article>
          ))}
        </div>
      </div>

      <div className="admin-filter-panel">
        <select onChange={(e) => console.log("예약 상태 필터", e.target.value)}>
          <option>전체 상태</option>
          <option>예약완료</option>
          <option>충전중</option>
          <option>완료</option>
          <option>취소</option>
          <option>노쇼</option>
        </select>
        <select onChange={(e) => console.log("예약 검색 유형", e.target.value)}>
          <option>전체 검색</option>
          <option>회원명</option>
          <option>충전소</option>
          <option>예약번호</option>
        </select>
        <input type="date" onChange={(e) => console.log("예약 시작일", e.target.value)} />
        <input type="date" onChange={(e) => console.log("예약 종료일", e.target.value)} />
        <input type="text" placeholder="검색어를 입력하세요" onChange={(e) => console.log("예약 검색어", e.target.value)} />
        <button type="button" onClick={() => console.log("예약 검색 클릭")}>검색</button>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>예약 목록</strong>
          <span>총 330건</span>
        </div>
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>예약번호</th>
                <th>예약시간</th>
                <th>회원명</th>
                <th>차량</th>
                <th>충전소</th>
                <th>충전기</th>
                <th>SOC</th>
                <th>예상시간</th>
                <th>예상금액</th>
                <th>인증코드</th>
                <th>상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((item) => (
                <tr key={item.id} onClick={() => goDetail(item.id)} className="admin-clickable-row">
                  <td>{item.id}</td>
                  <td>{item.time}</td>
                  <td>{item.member}</td>
                  <td>{item.vehicle}</td>
                  <td>{item.station}</td>
                  <td>{item.charger}</td>
                  <td>{item.soc}</td>
                  <td>{item.minutes}</td>
                  <td>{item.cost}</td>
                  <td>{item.code}</td>
                  <td><em className={`admin-badge ${item.status === "노쇼" ? "purple" : "green"}`}>{item.status}</em></td>
                  <td><button type="button" onClick={(e) => { e.stopPropagation(); goDetail(item.id); }}>상세</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
};

export default AdminReservationPage;
