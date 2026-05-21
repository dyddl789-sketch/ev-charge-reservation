<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 회원 목록</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/member.css">
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <!-- 본문 -->
        <main class="admin-content">

            <section class="admin-title-row">
                <div>
                    <h1>회원 목록</h1>
                    <p>가입한 회원 정보를 확인하고 회원 상태를 관리합니다.</p>
                </div>

                <form action="/admin/member/list" method="get" class="date-box">
                    <input type="date" id="memberDate" name="date" value="2026-05-20">
                </form>
            </section>

            <!-- 요약 카드 -->
            <section class="member-summary">

                <article class="member-summary-card">
                    <div>
                        <span>전체 회원 수</span>
                        <strong>28,745명</strong>
                    </div>
                </article>

                <article class="member-summary-card">
                    <div>
                        <span>회원</span>
                        <strong>27,812명</strong>
                    </div>
                </article>

                <article class="member-summary-card">
                    <div>
                        <span>탈퇴 회원</span>
                        <strong>918명</strong>
                    </div>
                </article>

            </section>

            <!-- 검색 영역 -->
            <section class="member-search-card">

                <div class="search-row">
                    <div class="search-group">
                        <label for="memberStatus">회원 상태</label>
                        <select id="memberStatus" name="status">
                            <option value="">전체</option>
                            <option value="ACTIVE">회원</option>
                            <option value="WITHDRAW">탈퇴 회원</option>
                        </select>
                    </div>

                    <div class="search-group">
                        <label for="joinStart">가입일</label>
                        <input type="date" id="joinStart" value="2026-04-20">
                    </div>

                    <span class="date-wave">~</span>

                    <div class="search-group">
                        <label for="joinEnd">종료일</label>
                        <input type="date" id="joinEnd" value="2026-05-20">
                    </div>

                    <div class="search-actions">
                        <button type="button" class="reset-btn">초기화</button>
                    </div>
                </div>

                <div class="search-row second">
                    <div class="search-group">
                        <label for="searchType">검색 기준</label>
                        <select id="searchType">
                            <option value="name">이름</option>
                            <option value="id">아이디</option>
                            <option value="email">이메일</option>
                            <option value="phone">연락처</option>
                        </select>
                    </div>

                    <div class="keyword-box">
                        <input type="text" placeholder="검색어를 입력하세요.">
                        <button type="button">검색</button>
                    </div>
                </div>

            </section>

            <!-- 회원 목록 테이블 -->
            <section class="member-table-card">

                <div class="table-top">
                    <p>총 <strong>28,745건</strong></p>

                    <select>
                        <option>10개씩 보기</option>
                        <option>20개씩 보기</option>
                        <option>50개씩 보기</option>
                    </select>
                </div>

                <table class="member-table">
                    <thead>
                        <tr>
                            <th><input type="checkbox"></th>
                            <th>번호</th>
                            <th>회원 구분</th>
                            <th>아이디</th>
                            <th>이름</th>
                            <th>이메일</th>
                            <th>연락처</th>
                            <th>가입일</th>
                            <th>상태</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td><input type="checkbox"></td>
                            <td>28745</td>
                            <td><span class="member-type user">회원</span></td>
                            <td>kms0520</td>
                            <td>김민수</td>
                            <td>kms0520@example.com</td>
                            <td>010-1234-5678</td>
                            <td>2026-05-20</td>
                            <td><span class="status-badge active">활성</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="withdraw-btn">탈퇴처리</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>28744</td>
                            <td><span class="member-type user">회원</span></td>
                            <td>lsoyeon</td>
                            <td>이소연</td>
                            <td>lsoyeon@example.com</td>
                            <td>010-2345-6789</td>
                            <td>2026-05-20</td>
                            <td><span class="status-badge active">활성</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="withdraw-btn">탈퇴처리</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>28743</td>
                            <td><span class="member-type user">회원</span></td>
                            <td>parkjh</td>
                            <td>박정훈</td>
                            <td>parkjh@example.com</td>
                            <td>010-3456-7890</td>
                            <td>2026-05-19</td>
                            <td><span class="status-badge active">활성</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="withdraw-btn">탈퇴처리</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>28742</td>
                            <td><span class="member-type user">회원</span></td>
                            <td>gildong</td>
                            <td>홍길동</td>
                            <td>gildong@example.com</td>
                            <td>010-6789-0123</td>
                            <td>2026-05-18</td>
                            <td><span class="status-badge withdraw">탈퇴</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="restore-btn">복구</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>28741</td>
                            <td><span class="member-type user">회원</span></td>
                            <td>skyhan</td>
                            <td>한지민</td>
                            <td>skyhan@example.com</td>
                            <td>010-7890-1234</td>
                            <td>2026-05-18</td>
                            <td><span class="status-badge active">활성</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="withdraw-btn">탈퇴처리</button>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination">
                    <button type="button">«</button>
                    <button type="button">‹</button>
                    <button type="button" class="active">1</button>
                    <button type="button">2</button>
                    <button type="button">3</button>
                    <button type="button">4</button>
                    <button type="button">5</button>
                    <button type="button">›</button>
                    <button type="button">»</button>
                </div>

            </section>

        </main>

    </div>

</div>

<script>
    const withdrawButtons = document.querySelectorAll(".withdraw-btn");

    withdrawButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            const result = confirm("해당 회원을 탈퇴 처리하시겠습니까?");

            if (!result) {
                return;
            }

            alert("회원이 탈퇴 처리되었습니다.");
        });
    });

    const restoreButtons = document.querySelectorAll(".restore-btn");

    restoreButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            alert("회원이 복구되었습니다.");
        });
    });
</script>

</body>
</html>