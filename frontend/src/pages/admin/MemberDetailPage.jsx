import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";

const DETAIL_PAGE_SIZE = 10;
const DEFAULT_PROFILE_IMAGE = "/images/member/profile/default-profile.png";

const tabMeta = {
  vehicles: { label: "등록 차량", emptyText: "등록 차량이 없습니다." },
  reservations: { label: "예약 내역", emptyText: "예약 내역이 없습니다." },
  charging: { label: "충전 완료", emptyText: "충전 완료 내역이 없습니다." },
  payments: { label: "총 이용금액", emptyText: "이용 금액 내역이 없습니다." },
};

const initialTabFilters = {
  reservations: {
    startDate: "",
    endDate: "",
    stationKeyword: "",
  },
  charging: {
    startDate: "",
    endDate: "",
    stationKeyword: "",
  },
};

const getBadgeColor = (value) => {
  if (value === "ACTIVE") return "green";
  if (value === "INACTIVE") return "warning";
  if (value === "BLOCKED") return "danger";
  if (value === "ADMIN" || value === "MANAGER") return "purple";
  if (value === "OPERATOR" || value === "ENGINEER") return "blue";
  return "blue";
};

const getStatusText = (value) => {
  if (value === "ACTIVE") return "활성";
  if (value === "INACTIVE") return "비활성";
  if (value === "BLOCKED") return "정지";
  return value || "-";
};

const getUserTypeText = (value) => {
  if (value === "ADMIN") return "최고관리자";
  if (value === "MANAGER") return "운영관리자";
  if (value === "OPERATOR") return "운영담당자";
  if (value === "ENGINEER") return "시설관리담당자";
  return "일반회원";
};

const formatDateTime = (value) => {
  if (!value) return "-";
  if (typeof value === "string") return value.replace("T", " ").slice(0, 16);
  return "-";
};

const getDatePart = (value) => {
  if (!value) return "";
  if (typeof value === "string") return value.replace("T", " ").slice(0, 10);
  return "";
};

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");
const moneyText = (value) => `${numberText(value)}원`;

const resolveProfileImageUrl = (profileImageUrl) => {
  console.log("관리자 회원 프로필 이미지 URL 변환", profileImageUrl);

  if (!profileImageUrl) return DEFAULT_PROFILE_IMAGE;
  if (profileImageUrl.startsWith("http")) return profileImageUrl;
  if (profileImageUrl.startsWith("/")) return profileImageUrl;
  return `/${profileImageUrl}`;
};

const normalizeDetail = (source = {}) => ({
  memberId: source.memberId,
  userId: source.userId ?? "-",
  memberName: source.memberName ?? "-",
  nickname: source.nickname ?? "-",
  phone: source.phone ?? "-",
  email: source.email ?? "-",
  profileImageUrl: source.profileImageUrl ?? "",
  userType: source.userType ?? "USER",
  loginType: source.loginType ?? "-",
  status: source.status ?? "ACTIVE",
  createdAtText: source.createdAtText ?? formatDateTime(source.createdAt),
  lastLoginAtText: source.lastLoginAtText ?? formatDateTime(source.lastLoginAt),
  updatedAtText: source.updatedAtText ?? formatDateTime(source.updatedAt),
  vehicleCount: source.vehicleCount ?? 0,
  reservationCount: source.reservationCount ?? 0,
  completedSessionCount: source.completedSessionCount ?? 0,
  totalPaymentAmount: source.totalPaymentAmount ?? 0,
});

const filterReservations = (reservationList, filters) => {
  console.log("회원 예약 내역 프론트 필터 적용", filters);

  return reservationList.filter((item) => {
    const stationName = item.stationName || "";
    const startDate = getDatePart(item.startTimeText || item.startTime);
    const matchesStation = !filters.stationKeyword || stationName.includes(filters.stationKeyword.trim());
    const matchesStart = !filters.startDate || startDate >= filters.startDate;
    const matchesEnd = !filters.endDate || startDate <= filters.endDate;

    return matchesStation && matchesStart && matchesEnd;
  });
};

const filterCharging = (chargingList, filters) => {
  console.log("회원 충전 완료 내역 프론트 필터 적용", filters);

  return chargingList.filter((item) => {
    const stationName = item.stationName || "";
    const startDate = getDatePart(item.actualStartTimeText || item.actualStartTime || item.actualEndTimeText || item.actualEndTime);
    const matchesStation = !filters.stationKeyword || stationName.includes(filters.stationKeyword.trim());
    const matchesStart = !filters.startDate || startDate >= filters.startDate;
    const matchesEnd = !filters.endDate || startDate <= filters.endDate;

    return matchesStation && matchesStart && matchesEnd;
  });
};

const MemberDetailPage = () => {
  console.log("MemberDetailPage 렌더링");

  const { memberId } = useParams();
  const navigate = useNavigate();

  const [detail, setDetail] = useState(null);
  const [vehicleList, setVehicleList] = useState([]);
  const [reservationList, setReservationList] = useState([]);
  const [chargingList, setChargingList] = useState([]);
  const [paymentList, setPaymentList] = useState([]);
  const [activeTab, setActiveTab] = useState("vehicles");
  const [tabPage, setTabPage] = useState({ vehicles: 1, reservations: 1, charging: 1, payments: 1 });
  const [tabFilters, setTabFilters] = useState(initialTabFilters);
  const [loading, setLoading] = useState(true);

  const loadDetail = async () => {
    console.log("회원 상세 실제 조회", memberId);
    setLoading(true);

    try {
      const response = await adminApi.member(memberId);
      console.log("회원 상세 응답", response.data);

      const data = response.data || {};
      setDetail(normalizeDetail(data.memberDetail));
      setVehicleList(data.vehicleList || []);
      setReservationList(data.reservationList || []);
      setChargingList(data.chargingList || []);
      setPaymentList(data.paymentList || []);
      setTabPage({ vehicles: 1, reservations: 1, charging: 1, payments: 1 });
      setTabFilters(initialTabFilters);
    } catch (error) {
      console.log("회원 상세 조회 실패", error);
      alert("회원 상세 정보를 불러오지 못했습니다.");
      setDetail(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [memberId]);

  const filteredReservationList = useMemo(
    () => filterReservations(reservationList, tabFilters.reservations),
    [reservationList, tabFilters.reservations],
  );

  const filteredChargingList = useMemo(
    () => filterCharging(chargingList, tabFilters.charging),
    [chargingList, tabFilters.charging],
  );

  const tabDataMap = useMemo(() => ({
    vehicles: vehicleList,
    reservations: filteredReservationList,
    charging: filteredChargingList,
    payments: paymentList,
  }), [vehicleList, filteredReservationList, filteredChargingList, paymentList]);

  const activeList = tabDataMap[activeTab] || [];
  const activePage = tabPage[activeTab] || 1;
  const activeTotalPage = Math.max(1, Math.ceil(activeList.length / DETAIL_PAGE_SIZE));
  const activePagedList = activeList.slice((activePage - 1) * DETAIL_PAGE_SIZE, activePage * DETAIL_PAGE_SIZE);

  const getTabValue = (tabName) => {
    if (tabName === "payments") return moneyText(detail?.totalPaymentAmount);
    if (tabName === "reservations") return numberText(filteredReservationList.length);
    if (tabName === "charging") return numberText(filteredChargingList.length);
    return numberText(tabDataMap[tabName]?.length || 0);
  };

  const changeTab = (tabName) => {
    console.log("회원 상세 탭 변경", tabName);
    setActiveTab(tabName);
  };

  const moveTabPage = (direction) => {
    console.log("회원 상세 탭 페이지 변경", activeTab, direction);
    setTabPage((prev) => {
      const currentPage = prev[activeTab] || 1;
      const nextPage = direction === "prev"
        ? Math.max(1, currentPage - 1)
        : Math.min(activeTotalPage, currentPage + 1);

      return {
        ...prev,
        [activeTab]: nextPage,
      };
    });
  };

  const changeTabFilter = (filterGroup, e) => {
    const { name, value } = e.target;
    console.log("회원 상세 탭 검색 조건 변경", filterGroup, name, value);

    setTabFilters((prev) => ({
      ...prev,
      [filterGroup]: {
        ...prev[filterGroup],
        [name]: value,
      },
    }));

    setTabPage((prev) => ({ ...prev, [filterGroup]: 1 }));
  };

  const resetTabFilter = (filterGroup) => {
    console.log("회원 상세 탭 검색 초기화", filterGroup);

    setTabFilters((prev) => ({
      ...prev,
      [filterGroup]: initialTabFilters[filterGroup],
    }));

    setTabPage((prev) => ({ ...prev, [filterGroup]: 1 }));
  };

  const renderTabFilter = () => {
    if (activeTab !== "reservations" && activeTab !== "charging") return null;

    const filterGroup = activeTab;
    const filters = tabFilters[filterGroup];
    const label = activeTab === "reservations" ? "예약일" : "충전일";

    return (
      <div className="admin-member-tab-filter">
        <label>
          <span>{label} 시작</span>
          <input
            type="date"
            name="startDate"
            value={filters.startDate}
            onChange={(e) => changeTabFilter(filterGroup, e)}
          />
        </label>
        <label>
          <span>{label} 종료</span>
          <input
            type="date"
            name="endDate"
            value={filters.endDate}
            onChange={(e) => changeTabFilter(filterGroup, e)}
          />
        </label>
        <label className="wide">
          <span>충전소명</span>
          <input
            type="text"
            name="stationKeyword"
            value={filters.stationKeyword}
            placeholder="충전소명으로 검색"
            onChange={(e) => changeTabFilter(filterGroup, e)}
          />
        </label>
        <button type="button" onClick={() => resetTabFilter(filterGroup)}>검색 초기화</button>
      </div>
    );
  };

  const renderTabPagination = () => (
    <div className="admin-tab-pagination">
      <span>10개 단위 · 현재 {activePage} / {activeTotalPage} 페이지 · 총 {numberText(activeList.length)}건</span>
      <div>
        <button type="button" disabled={activePage <= 1} onClick={() => moveTabPage("prev")}>이전</button>
        <button type="button" disabled={activePage >= activeTotalPage} onClick={() => moveTabPage("next")}>다음</button>
      </div>
    </div>
  );

  if (loading) {
    return <section className="admin-page"><div className="admin-panel">회원 상세 정보를 불러오는 중입니다.</div></section>;
  }

  if (!detail) {
    return (
      <section className="admin-page">
        <div className="admin-panel">
          <p className="admin-empty-text">회원 정보를 찾을 수 없습니다.</p>
          <button type="button" onClick={() => navigate("/admin/members")}>목록으로</button>
        </div>
      </section>
    );
  }

  return (
    <section className="admin-page admin-member-detail-page">
      <div className="admin-page-header">
        <div>
          <p>회원관리</p>
          <h1>회원 상세보기</h1>
          <span>회원 프로필, 기본 정보, 차량, 예약, 충전 완료, 총 이용금액을 실제 DB 기준으로 확인합니다.</span>
        </div>
        <button type="button" onClick={() => navigate("/admin/members")}>목록으로</button>
      </div>

      <div className="admin-member-detail-hero clean">
        <aside className="admin-member-photo-card photo-only">
          <img
            src={resolveProfileImageUrl(detail.profileImageUrl)}
            alt={`${detail.memberName} 프로필`}
            onError={(e) => {
              console.log("회원 프로필 이미지 로드 실패, 기본 이미지로 대체");
              e.currentTarget.src = DEFAULT_PROFILE_IMAGE;
            }}
          />
        </aside>

        <div className="admin-member-resume-card">
          <div className="admin-member-resume-head refined">
            <div>
              <span>Member Profile</span>
              <h2>{detail.memberName}</h2>
              <p>로그인 아이디: {detail.userId}</p>
              <div className="admin-member-head-badges">
                <em className={`admin-badge ${getBadgeColor(detail.status)}`}>{getStatusText(detail.status)}</em>
                <em className={`admin-badge ${getBadgeColor(detail.userType)}`}>{getUserTypeText(detail.userType)}</em>
              </div>
            </div>
            <strong>#{detail.memberId}</strong>
          </div>

          <div className="admin-member-contact-grid refined">
            <article>
              <span>회원번호</span>
              <strong>{detail.memberId}</strong>
            </article>
            <article>
              <span>회원명</span>
              <strong>{detail.memberName}</strong>
            </article>
            <article>
              <span>아이디</span>
              <strong>{detail.userId}</strong>
            </article>
            <article>
              <span>닉네임</span>
              <strong>{detail.nickname}</strong>
            </article>
            <article>
              <span>이메일</span>
              <strong>{detail.email}</strong>
            </article>
            <article>
              <span>휴대폰번호</span>
              <strong>{detail.phone}</strong>
            </article>
            <article>
              <span>로그인 타입</span>
              <strong>{detail.loginType}</strong>
            </article>
            <article>
              <span>가입일</span>
              <strong>{detail.createdAtText}</strong>
            </article>
            <article>
              <span>마지막 로그인</span>
              <strong>{detail.lastLoginAtText}</strong>
            </article>
          </div>
        </div>
      </div>

      <div className="admin-panel admin-member-tab-panel">
        <div className="admin-tab-row member-tabs refined-tabs">
          {Object.keys(tabMeta).map((tabName) => (
            <button
              type="button"
              key={tabName}
              className={activeTab === tabName ? "active" : ""}
              onClick={() => changeTab(tabName)}
            >
              <span>{tabMeta[tabName].label}</span>
              <b>{getTabValue(tabName)}</b>
            </button>
          ))}
        </div>

        <div className="admin-tab-panel-head">
          <div>
            <strong>{tabMeta[activeTab].label}</strong>
            <p>각 내역은 10개씩 표시하고 다음 페이지로 이동할 수 있습니다.</p>
          </div>
          {renderTabPagination()}
        </div>

        {renderTabFilter()}

        {activeTab === "vehicles" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table admin-member-detail-table">
              <thead><tr><th>차량번호</th><th>별칭</th><th>모델</th><th>차량번호판</th><th>대표</th><th>등록일</th></tr></thead>
              <tbody>
                {activePagedList.map((item) => (
                  <tr key={item.vehicleId}>
                    <td>{item.vehicleId}</td>
                    <td>{item.vehicleNickname || "-"}</td>
                    <td>{item.manufacturer} {item.modelName}</td>
                    <td>{item.plateNumber || "-"}</td>
                    <td>{item.isDefault ? <em className="admin-badge green">대표</em> : "-"}</td>
                    <td>{item.createdAtText || formatDateTime(item.createdAt)}</td>
                  </tr>
                ))}
                {activePagedList.length === 0 && <tr><td colSpan="6">{tabMeta.vehicles.emptyText}</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "reservations" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table admin-member-detail-table">
              <thead><tr><th>예약번호</th><th>충전소</th><th>충전기</th><th>차량</th><th>예약시간</th><th>상태</th><th>예상금액</th></tr></thead>
              <tbody>
                {activePagedList.map((item) => (
                  <tr key={item.reservationId}>
                    <td>RSV-{String(item.reservationId).padStart(6, "0")}</td>
                    <td>{item.stationName}</td>
                    <td>{item.chargerName}</td>
                    <td>{item.vehicleNickname || item.modelName}</td>
                    <td>{item.startTimeText || formatDateTime(item.startTime)}</td>
                    <td><em className="admin-badge blue">{item.status}</em></td>
                    <td>{moneyText(item.estimatedCost)}</td>
                  </tr>
                ))}
                {activePagedList.length === 0 && <tr><td colSpan="7">{tabMeta.reservations.emptyText}</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "charging" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table admin-member-detail-table">
              <thead><tr><th>세션번호</th><th>충전소</th><th>충전기</th><th>차량</th><th>시작</th><th>종료</th><th>충전량</th><th>금액</th></tr></thead>
              <tbody>
                {activePagedList.map((item) => (
                  <tr key={item.sessionId}>
                    <td>{item.sessionId}</td>
                    <td>{item.stationName}</td>
                    <td>{item.chargerName}</td>
                    <td>{item.vehicleNickname || item.modelName}</td>
                    <td>{item.actualStartTimeText || formatDateTime(item.actualStartTime)}</td>
                    <td>{item.actualEndTimeText || formatDateTime(item.actualEndTime)}</td>
                    <td>{numberText(item.actualKwh)}kWh</td>
                    <td>{moneyText(item.actualCost)}</td>
                  </tr>
                ))}
                {activePagedList.length === 0 && <tr><td colSpan="8">{tabMeta.charging.emptyText}</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "payments" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table admin-member-detail-table">
              <thead><tr><th>세션번호</th><th>예약번호</th><th>충전소</th><th>충전량</th><th>이용금액</th><th>완료일시</th><th>상태</th></tr></thead>
              <tbody>
                {activePagedList.map((item) => (
                  <tr key={item.sessionId}>
                    <td>{item.sessionId}</td>
                    <td>RSV-{String(item.reservationId).padStart(6, "0")}</td>
                    <td>{item.stationName}</td>
                    <td>{numberText(item.actualKwh)}kWh</td>
                    <td>{moneyText(item.actualCost)}</td>
                    <td>{item.paidAtText || formatDateTime(item.paidAt)}</td>
                    <td><em className="admin-badge green">{item.status}</em></td>
                  </tr>
                ))}
                {activePagedList.length === 0 && <tr><td colSpan="7">{tabMeta.payments.emptyText}</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {renderTabPagination()}
      </div>
    </section>
  );
};

export default MemberDetailPage;
