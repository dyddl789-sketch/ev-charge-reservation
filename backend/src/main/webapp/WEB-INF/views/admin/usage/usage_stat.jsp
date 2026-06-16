<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>관리자 - 이용 통계</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/usage_stat.css">

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

        <div class="admin-page-header">
            <div>
                <h1>이용 통계</h1>
                <p>실제 충전 완료 데이터를 기준으로 이용 현황을 조회합니다.</p>
            </div>
        </div>

        <!-- 검색 기간 -->
        <section class="stat-filter-section">
            <form method="get"
                  action="${pageContext.request.contextPath}/admin/usage"
                  class="stat-filter-form">

                <label>조회 기간</label>

                <input type="date" name="startDate" value="${startDate}">
                <span>~</span>
                <input type="date" name="endDate" value="${endDate}">

                <button type="submit">조회</button>
            </form>
        </section>

        <!-- 요약 카드 -->
        <section class="stat-summary-grid">

            <div class="stat-card">
                <p class="stat-label">총 이용 건수</p>
                <strong class="stat-value">
                    <fmt:formatNumber value="${usageStat.summary.totalUsageCount}" pattern="#,###" />건
                </strong>
            </div>

            <div class="stat-card">
                <p class="stat-label">오늘 이용 건수</p>
                <strong class="stat-value">
                    <fmt:formatNumber value="${usageStat.summary.todayUsageCount}" pattern="#,###" />건
                </strong>
            </div>

            <div class="stat-card">
                <p class="stat-label">평균 충전 시간</p>
                <strong class="stat-value">
                    <fmt:formatNumber value="${usageStat.summary.avgChargingMinutes}" pattern="#,###" />분
                </strong>
            </div>

            <div class="stat-card">
                <p class="stat-label">총 충전량</p>
                <strong class="stat-value">
                    <fmt:formatNumber value="${usageStat.summary.totalKwh}" pattern="#,##0.0" />kWh
                </strong>
            </div>

        </section>

        <!-- 차트 영역 -->
        <section class="stat-chart-grid">

            <div class="stat-panel">
                <div class="stat-panel-header">
                    <h2>일별 이용 현황</h2>
                    <p>선택 기간 내 일자별 충전 완료 건수</p>
                </div>
                <canvas id="dailyUsageChart"></canvas>
            </div>

            <div class="stat-panel">
                <div class="stat-panel-header">
                    <h2>시간대별 이용 현황</h2>
                    <p>선택 기간 내 시간대별 충전 완료 건수</p>
                </div>
                <canvas id="hourlyUsageChart"></canvas>
            </div>

        </section>

        <!-- 충전 타입별 이용 현황 -->
        <section class="stat-panel">
            <div class="stat-panel-header">
                <h2>충전 타입별 이용 현황</h2>
                <p>완속 / 급속 / 초급속 기준 이용 비율</p>
            </div>

            <table class="stat-table">
                <thead>
                    <tr>
                        <th>충전 타입</th>
                        <th>이용 건수</th>
                        <th>비율</th>
                        <th>평균 시간</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="type" items="${usageStat.typeList}">
                        <tr>
                            <td>${type.chargerType}</td>
                            <td>
                                <fmt:formatNumber value="${type.usageCount}" pattern="#,###" />건
                            </td>
                            <td>${type.usageRate}%</td>
                            <td>${type.avgMinutes}분</td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty usageStat.typeList}">
                        <tr>
                            <td colspan="4" class="empty-cell">
                                조회된 이용 데이터가 없습니다.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </section>

        <!-- 충전소별 이용 순위 -->
        <section class="stat-panel">
            <div class="stat-panel-header">
                <h2>충전소별 이용 순위</h2>
                <p>충전 완료 건수 기준 상위 충전소</p>
            </div>

            <table class="stat-table">
                <thead>
                    <tr>
                        <th>순위</th>
                        <th>충전소명</th>
                        <th>이용 건수</th>
                        <th>평균 시간</th>
                        <th>이용 비율</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="station" items="${usageStat.stationRankList}">
                        <tr>
                            <td>${station.rankNo}</td>
                            <td>${station.stationName}</td>
                            <td>
                                <fmt:formatNumber value="${station.usageCount}" pattern="#,###" />건
                            </td>
                            <td>${station.avgMinutes}분</td>
                            <td>${station.operationRate}%</td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty usageStat.stationRankList}">
                        <tr>
                            <td colspan="5" class="empty-cell">
                                조회된 충전소 이용 데이터가 없습니다.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </section>

    	</main>
    </div>
</div>

<script>
    // ==============================
    // 일별 이용 현황 차트 데이터
    // ==============================
    const dailyUsageLabels = [
        <c:forEach var="daily" items="${usageStat.dailyList}" varStatus="status">
            "${daily.usageDate}"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    const dailyUsageData = [
        <c:forEach var="daily" items="${usageStat.dailyList}" varStatus="status">
            ${daily.usageCount}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    // ==============================
    // 시간대별 이용 현황 차트 데이터
    // ==============================
    const hourlyUsageLabels = [
        <c:forEach var="hourly" items="${usageStat.hourlyList}" varStatus="status">
            "${hourly.usageHour}시"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    const hourlyUsageData = [
        <c:forEach var="hourly" items="${usageStat.hourlyList}" varStatus="status">
            ${hourly.usageCount}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    Chart.defaults.font.family = "'Pretendard', 'Noto Sans KR', 'Malgun Gothic', sans-serif";
    Chart.defaults.color = '#64748b';

    const commonUsageTooltip = {
        backgroundColor: '#111827',
        titleColor: '#ffffff',
        bodyColor: '#ffffff',
        padding: 12,
        cornerRadius: 10,
        callbacks: {
            label: function (context) {
                return context.dataset.label + ' ' + Number(context.raw || 0).toLocaleString() + '건';
            }
        }
    };

    // ==============================
    // 일별 이용 현황 차트
    // ==============================
    const dailyUsageCanvas = document.getElementById('dailyUsageChart');

    if (dailyUsageCanvas) {
        const dailyContext = dailyUsageCanvas.getContext('2d');

        const dailyGradient = dailyContext.createLinearGradient(0, 0, 0, 320);
        dailyGradient.addColorStop(0, 'rgba(59, 130, 246, 0.32)');
        dailyGradient.addColorStop(0.6, 'rgba(59, 130, 246, 0.10)');
        dailyGradient.addColorStop(1, 'rgba(59, 130, 246, 0.00)');

        new Chart(dailyUsageCanvas, {
            type: 'line',
            data: {
                labels: dailyUsageLabels,
                datasets: [{
                    label: '이용 건수',
                    data: dailyUsageData,
                    borderColor: '#2563eb',
                    backgroundColor: dailyGradient,
                    pointBackgroundColor: '#ffffff',
                    pointBorderColor: '#2563eb',
                    pointBorderWidth: 3,
                    pointRadius: 4,
                    pointHoverRadius: 7,
                    borderWidth: 3,
                    tension: 0.38,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: commonUsageTooltip
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            maxRotation: 0,
                            autoSkip: true,
                            maxTicksLimit: 8
                        }
                    },
                    y: {
                        beginAtZero: true,
                        border: {
                            display: false
                        },
                        grid: {
                            color: 'rgba(148, 163, 184, 0.22)'
                        },
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    // ==============================
    // 시간대별 이용 현황 차트
    // ==============================
    const hourlyUsageCanvas = document.getElementById('hourlyUsageChart');

    if (hourlyUsageCanvas) {
        const hourlyContext = hourlyUsageCanvas.getContext('2d');

        const hourlyGradient = hourlyContext.createLinearGradient(0, 0, 0, 320);
        hourlyGradient.addColorStop(0, 'rgba(16, 185, 129, 0.90)');
        hourlyGradient.addColorStop(1, 'rgba(59, 130, 246, 0.85)');

        new Chart(hourlyUsageCanvas, {
            type: 'bar',
            data: {
                labels: hourlyUsageLabels,
                datasets: [{
                    label: '이용 건수',
                    data: hourlyUsageData,
                    backgroundColor: hourlyGradient,
                    borderColor: 'rgba(255, 255, 255, 0)',
                    borderWidth: 0,
                    borderRadius: 14,
                    borderSkipped: false,
                    maxBarThickness: 54
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: commonUsageTooltip
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            font: {
                                weight: '700'
                            }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        border: {
                            display: false
                        },
                        grid: {
                            color: 'rgba(148, 163, 184, 0.22)'
                        },
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }
</script>

</body>
</html>