import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";

// 백엔드 REST API 연결 전 화면 확인용 Mock 데이터
const mockMembers = [
  {
    memberId: 85,
    userId: "demo_user_30",
    memberName: "시연회원30",
    email: "demo_user_30@evcharge.test",
    phone: "010-8830-1030",
    userType: "USER",
    loginType: "KAKAO",
    status: "ACTIVE",
    createdAtText: "2026-06-04",
    vehicleCount: 1,
    reservationCount: 0,
  },
  {
    memberId: 1,
    userId: "kakao_4916296150",
    memberName: "김성민",
    email: "dyddl456@nate.com",
    phone: "010-0000-0000",
    userType: "USER",
    loginType: "KAKAO",
    status: "ACTIVE",
    createdAtText: "2026-05-20",
    vehicleCount: 2,
    reservationCount: 12,
  },
  {
    memberId: 2,
    userId: "testuser01",
    memberName: "박회원",
    email: "park@test.com",
    phone: "010-1111-2222",
    userType: "USER",
    loginType: "LOCAL",
    status: "ACTIVE",
    createdAtText: "2026-05-18",
    vehicleCount: 1,
    reservationCount: 8,
  },
  {
    memberId: 3,
    userId: "operator01",
    memberName: "김운영",
    email: "operator@test.com",
    phone: "010-3333-4444",
    userType: "OPERATOR",
    loginType: "LOCAL",
    status: "ACTIVE",
    createdAtText: "2026-05-10",
    vehicleCount: 0,
    reservationCount: 0,
  },
];

const getBadgeColor = (value) => {
  if (value === "ACTIVE") return "green";
  if (value === "BLOCKED") return "danger";
  if (value === "INACTIVE") return "warning";
  if (value === "ADMIN" || value === "MANAGER") return "purple";
  if (value === "OPERATOR" || value === "ENGINEER") return "blue";
  return "blue";
};

const normalizeMember = (member) => ({
  memberId: member.memberId ?? member.id,
  userId: member.userId ?? "-",
  memberName: member.memberName ?? member.name ?? "-",
  email: member.email ?? "-",
  phone: member.phone ?? "-",
  userType: member.userType ?? member.type ?? "USER",
  loginType: member.loginType ?? "-",
  status: member.status ?? "ACTIVE",
  createdAtText: member.createdAtText ?? member.createdAt ?? "-",
  vehicleCount: member.vehicleCount ?? 0,
  reservationCount: member.reservationCount ?? 0,
});

const extractMemberRows = (data) => {
  if (Array.isArray(data)) return data.map(normalizeMember);
  if (Array.isArray(data?.members)) return data.members.map(normalizeMember);
  if (Array.isArray(data?.memberList)) return data.memberList.map(normalizeMember);
  if (Array.isArray(data?.content)) return data.content.map(normalizeMember);
  if (Array.isArray(data?.memberPage?.memberList)) return data.memberPage.memberList.map(normalizeMember);
  if (Array.isArray(data?.memberPage?.members)) return data.memberPage.members.map(normalizeMember);
  return [];
};

const MemberPage = () => {
  console.log("MemberPage 렌더링");

  const navigate = useNavigate();

  const [members, setMembers] = useState(mockMembers.map(normalizeMember));
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [userType, setUserType] = useState("");

  const loadMembers = async () => {
    console.log("회원 목록 조회 실행", { keyword, status, userType });

    try {
      const response = await adminApi.members({
        keyword,
        status,
        userType,
      });

      console.log("회원 목록 응답", response.data);

      // 기존 JSP Controller는 HTML을 반환할 수 있으므로 JSON일 때만 화면 데이터로 사용
      const rows = extractMemberRows(response.data);

      if (rows.length > 0) {
        setMembers(rows);
        return;
      }

      console.log("회원 목록 JSON 데이터 없음 - Mock 데이터 유지");
      setMembers(mockMembers.map(normalizeMember));
    } catch (error) {
      console.log("회원 목록 API 미연결 또는 오류 - Mock 데이터 사용", error);
      setMembers(mockMembers.map(normalizeMember));
    }
  };

  useEffect(() => {
    loadMembers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredMembers = useMemo(() => {
    return members.filter((member) => {
      const keywordMatched =
        !keyword ||
        member.userId.includes(keyword) ||
        member.memberName.includes(keyword) ||
        member.email.includes(keyword);

      const statusMatched = !status || member.status === status;
      const userTypeMatched = !userType || member.userType === userType;

      return keywordMatched && statusMatched && userTypeMatched;
    });
  }, [members, keyword, status, userType]);

  const goMemberDetail = (memberId) => {
    console.log("회원 상세 페이지 이동", memberId);
    navigate(`/admin/members/${memberId}`);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>회원관리</p>
          <h1>회원 목록</h1>
          <span>회원 기본 정보와 차량/예약 이용 현황을 확인합니다.</span>
        </div>
        <button type="button" onClick={loadMembers}>회원 조회</button>
      </div>

      <div className="admin-filter-panel">
        <select
          value={status}
          onChange={(e) => {
            console.log("회원 상태 선택", e.target.value);
            setStatus(e.target.value);
          }}
        >
          <option value="">전체 상태</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
          <option value="BLOCKED">BLOCKED</option>
        </select>

        <select
          value={userType}
          onChange={(e) => {
            console.log("회원 권한 선택", e.target.value);
            setUserType(e.target.value);
          }}
        >
          <option value="">전체 권한</option>
          <option value="USER">USER</option>
          <option value="OPERATOR">OPERATOR</option>
          <option value="ENGINEER">ENGINEER</option>
          <option value="MANAGER">MANAGER</option>
          <option value="ADMIN">ADMIN</option>
        </select>

        <input
          type="text"
          placeholder="아이디, 이름, 이메일 검색"
          value={keyword}
          onChange={(e) => {
            console.log("회원 검색어", e.target.value);
            setKeyword(e.target.value);
          }}
        />

        <button type="button" onClick={loadMembers}>검색</button>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <strong>회원 목록</strong>
          <span>총 {filteredMembers.length}명</span>
        </div>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>회원번호</th>
                <th>아이디</th>
                <th>이름</th>
                <th>이메일</th>
                <th>연락처</th>
                <th>권한</th>
                <th>상태</th>
                <th>가입일</th>
                <th>차량</th>
                <th>예약</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {filteredMembers.map((member) => (
                <tr
                  key={member.memberId}
                  className="admin-clickable-row"
                  onClick={() => goMemberDetail(member.memberId)}
                >
                  <td>{member.memberId}</td>
                  <td>{member.userId}</td>
                  <td>{member.memberName}</td>
                  <td>{member.email}</td>
                  <td>{member.phone}</td>
                  <td>
                    <em className={`admin-badge ${getBadgeColor(member.userType)}`}>
                      {member.userType}
                    </em>
                  </td>
                  <td>
                    <em className={`admin-badge ${getBadgeColor(member.status)}`}>
                      {member.status}
                    </em>
                  </td>
                  <td>{member.createdAtText}</td>
                  <td>{member.vehicleCount}대</td>
                  <td>{member.reservationCount}건</td>
                  <td>
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        goMemberDetail(member.memberId);
                      }}
                    >
                      상세
                    </button>
                  </td>
                </tr>
              ))}

              {filteredMembers.length === 0 && (
                <tr>
                  <td colSpan="11">조회된 회원이 없습니다.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
};

export default MemberPage;
