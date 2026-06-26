import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";

const today = new Date();
const todayText = today.toISOString().slice(0, 10);

const initialSearch = {
  status: "",
  userType: "",
  searchType: "all",
  keyword: "",
  joinStart: "",
  joinEnd: "",
};

const getBadgeColor = (value) => {
  if (value === "ACTIVE") return "green";
  if (value === "INACTIVE") return "warning";
  if (value === "ADMIN" || value === "MANAGER") return "purple";
  if (value === "OPERATOR" || value === "ENGINEER") return "blue";
  if (value === "BLOCKED") return "danger";
  return "blue";
};

const formatDate = (value) => {
  if (!value) return "-";
  if (typeof value === "string") return value.replace("T", " ").slice(0, 10);
  return "-";
};

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");

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

const normalizeMember = (member) => ({
  memberId: member.memberId,
  userId: member.userId ?? "-",
  memberName: member.memberName ?? "-",
  nickname: member.nickname ?? "-",
  email: member.email ?? "-",
  phone: member.phone ?? "-",
  userType: member.userType ?? "USER",
  loginType: member.loginType ?? "-",
  status: member.status ?? "ACTIVE",
  createdAtText: member.createdAtText ?? formatDate(member.createdAt),
});

const MemberPage = () => {
  console.log("MemberPage 렌더링");

  const navigate = useNavigate();

  const [members, setMembers] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 1, size: 20, totalPage: 1, searchCount: 0 });
  const [summary, setSummary] = useState({ totalCount: 0, activeCount: 0, inactiveCount: 0 });
  const [search, setSearch] = useState(initialSearch);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);

  const params = useMemo(() => ({
    status: search.status || undefined,
    userType: search.userType || undefined,
    searchType: search.searchType || undefined,
    keyword: search.keyword || undefined,
    joinStart: search.joinStart || undefined,
    joinEnd: search.joinEnd || undefined,
    page,
    size: 20,
  }), [search, page]);

  const loadMembers = async () => {
    console.log("회원 목록 실제 조회 실행", params);
    setLoading(true);

    try {
      const response = await adminApi.members(params);
      console.log("회원 목록 응답", response.data);

      const data = response.data || {};
      const rows = (data.memberList || []).map(normalizeMember);

      setMembers(rows);
      setSummary({
        totalCount: data.totalCount ?? 0,
        activeCount: data.activeCount ?? 0,
        inactiveCount: data.inactiveCount ?? 0,
      });
      setPageInfo({
        page: data.page ?? page,
        size: data.size ?? 20,
        totalPage: data.totalPage ?? 1,
        searchCount: data.searchCount ?? rows.length,
      });
    } catch (error) {
      console.log("회원 목록 조회 실패", error);
      alert("회원 목록을 불러오지 못했습니다. 로그인 권한과 백엔드 실행 상태를 확인해 주세요.");
      setMembers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMembers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const changeSearch = (e) => {
    const { name, value } = e.target;
    console.log("회원 검색 조건 변경", name, value);
    setSearch((prev) => ({ ...prev, [name]: value }));
  };

  const submitSearch = (e) => {
    e.preventDefault();
    console.log("회원 검색 실행", search);
    if (page !== 1) {
      setPage(1);
      return;
    }
    loadMembers();
  };

  const resetSearch = () => {
    console.log("회원 검색 초기화");
    setSearch(initialSearch);
    setPage(1);
  };

  const goMemberDetail = (targetMemberId) => {
    console.log("회원 상세 페이지 이동", targetMemberId);
    navigate(`/admin/members/${targetMemberId}`);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>회원관리</p>
          <h1>회원 목록</h1>
          <span>회원 식별에 필요한 정보만 목록에 표시하고, 예약·충전·이용금액은 상세 화면에서 확인합니다.</span>
        </div>
        <button type="button" onClick={loadMembers} disabled={loading}>{loading ? "조회 중" : "새로고침"}</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 회원</span><strong>{numberText(summary.totalCount)}명</strong><p>app_member 기준</p></article>
        <article className="admin-kpi-card"><span>활성 회원</span><strong>{numberText(summary.activeCount)}명</strong><p>ACTIVE 상태</p></article>
        <article className="admin-kpi-card"><span>비활성 회원</span><strong>{numberText(summary.inactiveCount)}명</strong><p>INACTIVE 상태</p></article>
        <article className="admin-kpi-card"><span>검색 결과</span><strong>{numberText(pageInfo.searchCount)}명</strong><p>현재 조건 기준</p></article>
      </div>

      <form className="admin-filter-panel polished" onSubmit={submitSearch}>
        <label>
          <span>상태</span>
          <select name="status" value={search.status} onChange={changeSearch}>
            <option value="">전체 상태</option>
            <option value="ACTIVE">활성</option>
            <option value="INACTIVE">비활성</option>
          </select>
        </label>
        <label>
          <span>권한</span>
          <select name="userType" value={search.userType} onChange={changeSearch}>
            <option value="">전체 권한</option>
            <option value="USER">일반회원</option>
            <option value="OPERATOR">운영담당자</option>
            <option value="ENGINEER">시설관리담당자</option>
            <option value="MANAGER">운영관리자</option>
            <option value="ADMIN">최고관리자</option>
          </select>
        </label>
        <label>
          <span>가입 시작</span>
          <input type="date" name="joinStart" value={search.joinStart} onChange={changeSearch} max={todayText} />
        </label>
        <label>
          <span>가입 종료</span>
          <input type="date" name="joinEnd" value={search.joinEnd} onChange={changeSearch} max={todayText} />
        </label>
        <label>
          <span>검색 기준</span>
          <select name="searchType" value={search.searchType} onChange={changeSearch}>
            <option value="all">전체</option>
            <option value="id">아이디</option>
            <option value="name">이름</option>
            <option value="nickname">닉네임</option>
            <option value="email">이메일</option>
            <option value="phone">휴대폰번호</option>
          </select>
        </label>
        <label className="wide">
          <span>검색어</span>
          <input
            type="text"
            name="keyword"
            value={search.keyword}
            placeholder="아이디, 이름, 닉네임, 이메일, 휴대폰번호 검색"
            onChange={changeSearch}
          />
        </label>
        <div className="filter-actions">
          <button type="submit">검색</button>
          <button type="button" className="gray" onClick={resetSearch}>초기화</button>
        </div>
      </form>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <div>
            <strong>회원 리스트</strong>
            <p>20개 단위 페이지네이션 · 현재 {pageInfo.page} / {pageInfo.totalPage} 페이지</p>
          </div>
        </div>

        <div className="admin-table-wrap elegant-table-wrap">
          <table className="admin-table elegant-table admin-member-list-table compact-member-table">
            <thead>
              <tr>
                <th>회원번호</th>
                <th>회원명</th>
                <th>아이디</th>
                <th>닉네임</th>
                <th>이메일</th>
                <th>권한</th>
                <th>상태</th>
                <th>가입일</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {members.map((member) => (
                <tr key={member.memberId} className="admin-clickable-row" onClick={() => goMemberDetail(member.memberId)}>
                  <td>{member.memberId}</td>
                  <td className="admin-text-left"><b>{member.memberName}</b></td>
                  <td className="admin-text-left"><span className="admin-member-id-text">{member.userId}</span></td>
                  <td>{member.nickname && member.nickname !== "-" ? member.nickname : "-"}</td>
                  <td className="admin-text-left">{member.email}</td>
                  <td><em className={`admin-badge ${getBadgeColor(member.userType)}`}>{getUserTypeText(member.userType)}</em></td>
                  <td><em className={`admin-badge ${getBadgeColor(member.status)}`}>{getStatusText(member.status)}</em></td>
                  <td>{member.createdAtText}</td>
                  <td><button type="button" onClick={(e) => { e.stopPropagation(); goMemberDetail(member.memberId); }}>상세</button></td>
                </tr>
              ))}
              {!loading && members.length === 0 && (
                <tr><td colSpan="9">조회된 회원이 없습니다.</td></tr>
              )}
              {loading && (
                <tr><td colSpan="9">회원 데이터를 불러오는 중입니다.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="admin-pagination">
          <button type="button" disabled={pageInfo.page <= 1 || loading} onClick={() => setPage((prev) => Math.max(1, prev - 1))}>이전</button>
          <span>{pageInfo.page} / {pageInfo.totalPage}</span>
          <button type="button" disabled={pageInfo.page >= pageInfo.totalPage || loading} onClick={() => setPage((prev) => prev + 1)}>다음</button>
        </div>
      </div>
    </section>
  );
};

export default MemberPage;
