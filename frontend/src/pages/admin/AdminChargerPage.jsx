const chargers = [
  { id: 1, station: "부산시청 공공충전소", name: "급속 01", type: "급속", connector: "DC콤보", speed: "100kW", price: "347원", status: "사용가능" },
  { id: 2, station: "해운대 공영주차장", name: "초급속 02", type: "초급속", connector: "DC콤보", speed: "300kW", price: "385원", status: "점검중" },
  { id: 3, station: "센텀시티 공영주차장", name: "완속 01", type: "완속", connector: "AC3상", speed: "7kW", price: "292원", status: "사용중" },
];

const AdminChargerPage = () => {
  console.log("AdminChargerPage 렌더링");

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>충전기관리</p>
          <h1>충전기 목록</h1>
          <span>충전기 타입, 커넥터, 출력, 요금, 실시간 운영 상태를 관리합니다.</span>
        </div>
        <button type="button" onClick={() => console.log("충전기 등록 클릭")}>충전기 등록</button>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>충전기 목록</strong>
          <span>총 {chargers.length}기</span>
        </div>
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>번호</th>
                <th>충전소</th>
                <th>충전기</th>
                <th>유형</th>
                <th>커넥터</th>
                <th>출력</th>
                <th>요금/kWh</th>
                <th>상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {chargers.map((charger) => (
                <tr key={charger.id}>
                  <td>{charger.id}</td>
                  <td>{charger.station}</td>
                  <td>{charger.name}</td>
                  <td>{charger.type}</td>
                  <td>{charger.connector}</td>
                  <td>{charger.speed}</td>
                  <td>{charger.price}</td>
                  <td><em className={`admin-badge ${charger.status === "사용가능" ? "green" : charger.status === "점검중" ? "warning" : "blue"}`}>{charger.status}</em></td>
                  <td><button type="button" onClick={() => console.log("충전기 상세", charger.id)}>관리</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
};

export default AdminChargerPage;
