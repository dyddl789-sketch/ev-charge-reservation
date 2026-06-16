<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>관리자 - 매출 통계</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sales_stat.css">

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels@2"></script>
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

        <div class="admin-page-header">
            <div>
                <h1>매출 통계</h1>
                <p>실제 충전 완료 데이터를 기준으로 매출 현황을 조회합니다.</p>
            </div>
        </div>

        <!-- 검색 기간 -->
        <section class="sales-filter-section">
            <form method="get"
                  action="${pageContext.request.contextPath}/admin/sales"
                  class="sales-filter-form">

                <label>조회 기간</label>

                <input type="date" name="startDate" value="${startDate}">
                <span>~</span>
                <input type="date" name="endDate" value="${endDate}">

                <button type="submit">조회</button>
            </form>
        </section>

        <!-- 요약 카드 -->
        <section class="sales-summary-grid">

            <div class="sales-card">
                <p class="sales-label">총 매출</p>
                <strong class="sales-value">
                    <fmt:formatNumber value="${salesStat.summary.totalSalesAmount}" pattern="#,###" />원
                </strong>
            </div>

            <div class="sales-card">
                <p class="sales-label">오늘 매출</p>
                <strong class="sales-value">
                    <fmt:formatNumber value="${salesStat.summary.todaySalesAmount}" pattern="#,###" />원
                </strong>
            </div>

            <div class="sales-card">
                <p class="sales-label">평균 결제 금액</p>
                <strong class="sales-value">
                    <fmt:formatNumber value="${salesStat.summary.avgPaymentAmount}" pattern="#,###" />원
                </strong>
            </div>

            <div class="sales-card">
                <p class="sales-label">총 충전량</p>
                <strong class="sales-value">
                    <fmt:formatNumber value="${salesStat.summary.totalKwh}" pattern="#,##0.0" />kWh
                </strong>
            </div>

        </section>

        <!-- 차트 영역 -->
        <section class="sales-chart-grid">

            <div class="sales-panel">
                <div class="sales-panel-header">
                    <h2>일별 매출 현황</h2>
                    <p>선택 기간 내 일자별 충전 완료 매출</p>
                </div>
                <canvas id="dailySalesChart"></canvas>
            </div>

            <div class="sales-panel">
                <div class="sales-panel-header">
                    <h2>충전 타입별 매출 비율</h2>
                    <p>완속 / 급속 / 초급속 기준 매출 비율</p>
                </div>
                <canvas id="salesTypeChart"></canvas>
            </div>

        </section>

        <!-- 충전 타입별 매출 현황 -->
        <section class="sales-panel">
            <div class="sales-panel-header">
                <h2>충전 타입별 매출 현황</h2>
                <p>충전기 유형별 매출 금액과 비율</p>
            </div>

            <table class="sales-table">
                <thead>
                    <tr>
                        <th>충전 타입</th>
                        <th>매출</th>
                        <th>비율</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="type" items="${salesStat.typeList}">
                        <tr>
                            <td>${type.chargerType}</td>
                            <td>
                                <fmt:formatNumber value="${type.salesAmount}" pattern="#,###" />원
                            </td>
							<td>
							    <span class="sales-rate-badge">
							        <fmt:formatNumber value="${type.salesRate}" pattern="#,##0.0" />%
							    </span>
							</td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty salesStat.typeList}">
                        <tr>
                            <td colspan="3" class="empty-cell">
                                조회된 매출 데이터가 없습니다.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </section>

        <!-- 충전소별 매출 순위 -->
        <section class="sales-panel">
            <div class="sales-panel-header">
                <h2>충전소별 매출 순위</h2>
                <p>충전 완료 매출 기준 상위 충전소</p>
            </div>

            <table class="sales-table">
                <thead>
                    <tr>
                        <th>순위</th>
                        <th>충전소명</th>
                        <th>매출</th>
                        <th>결제 건수</th>
                        <th>총 충전량</th>
                        <th>평균 결제 금액</th>
                        <th>비율</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="station" items="${salesStat.stationRankList}">
                        <tr>
                            <td>${station.rankNo}</td>
                            <td>${station.stationName}</td>
                            <td>
                                <fmt:formatNumber value="${station.salesAmount}" pattern="#,###" />원
                            </td>
                            <td>
                                <fmt:formatNumber value="${station.paymentCount}" pattern="#,###" />건
                            </td>
                            <td>
                                <fmt:formatNumber value="${station.totalKwh}" pattern="#,##0.0" />kWh
                            </td>
                            <td>
                                <fmt:formatNumber value="${station.avgPaymentAmount}" pattern="#,###" />원
                            </td>
                            <td>
							    <fmt:formatNumber value="${station.salesRate}" pattern="#,##0.0" />%
							</td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty salesStat.stationRankList}">
                        <tr>
                            <td colspan="7" class="empty-cell">
                                조회된 충전소 매출 데이터가 없습니다.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </section>

        <!-- 최근 매출 내역 -->
        <section class="sales-panel">
            <div class="sales-panel-header">
                <h2>최근 매출 내역</h2>
                <p>선택 기간 내 최근 충전 완료 결제 내역</p>
            </div>

            <table class="sales-table sales-history-table">
                <thead>
                    <tr>
                        <th>결제일시</th>
                        <th>회원명</th>
                        <th>충전소명</th>
                        <th>충전기명</th>
                        <th>충전량</th>
                        <th>결제금액</th>
                        <th>상태</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="history" items="${salesStat.historyList}">
                        <tr>
                            <td>${history.paymentDate}</td>
                            <td>${history.memberName}</td>
                            <td>${history.stationName}</td>
                            <td>${history.chargerName}</td>
                            <td>
                                <fmt:formatNumber value="${history.actualKwh}" pattern="#,##0.0" />kWh
                            </td>
                            <td>
                                <fmt:formatNumber value="${history.actualCost}" pattern="#,###" />원
                            </td>
                            <td>${history.status}</td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty salesStat.historyList}">
                        <tr>
                            <td colspan="7" class="empty-cell">
                                조회된 최근 매출 내역이 없습니다.
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
    const dailySalesLabels = [
        <c:forEach var="daily" items="${salesStat.dailyList}" varStatus="status">
            "${daily.salesDate}"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    const dailySalesData = [
        <c:forEach var="daily" items="${salesStat.dailyList}" varStatus="status">
            ${daily.salesAmount}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    const salesTypeLabels = [
        <c:forEach var="type" items="${salesStat.typeList}" varStatus="status">
            "${type.chargerType}"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    const salesTypeData = [
        <c:forEach var="type" items="${salesStat.typeList}" varStatus="status">
            ${type.salesAmount}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    const totalSalesAmount = Number("${salesStat.summary.totalSalesAmount}");

    const formatWon = function (value) {
        return Number(value || 0).toLocaleString() + '원';
    };

    Chart.defaults.font.family = "'Pretendard', 'Noto Sans KR', 'Malgun Gothic', sans-serif";
    Chart.defaults.color = '#64748b';

    if (typeof ChartDataLabels !== 'undefined') {
        Chart.register(ChartDataLabels);
    }

    const centerTextPlugin = {
        id: 'centerTextPlugin',
        afterDraw: function (chart) {
            if (chart.config.type !== 'doughnut') {
                return;
            }

            const ctx = chart.ctx;
            const chartArea = chart.chartArea;
            const centerX = (chartArea.left + chartArea.right) / 2;
            const centerY = (chartArea.top + chartArea.bottom) / 2;

            ctx.save();
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';

            ctx.fillStyle = '#64748b';
            ctx.font = '700 13px Pretendard, Noto Sans KR, sans-serif';
            ctx.fillText('총 매출', centerX, centerY - 12);

            ctx.fillStyle = '#111827';
            ctx.font = '900 20px Pretendard, Noto Sans KR, sans-serif';
            ctx.fillText(formatWon(totalSalesAmount), centerX, centerY + 14);

            ctx.restore();
        }
    };

    Chart.register(centerTextPlugin);

    const salesLineCanvas = document.getElementById('dailySalesChart');

    if (salesLineCanvas) {
        const lineContext = salesLineCanvas.getContext('2d');

        const salesGradient = lineContext.createLinearGradient(0, 0, 0, 320);
        salesGradient.addColorStop(0, 'rgba(240, 90, 0, 0.34)');
        salesGradient.addColorStop(0.55, 'rgba(240, 90, 0, 0.12)');
        salesGradient.addColorStop(1, 'rgba(240, 90, 0, 0.00)');

        new Chart(salesLineCanvas, {
            type: 'line',
            data: {
                labels: dailySalesLabels,
                datasets: [{
                    label: '일별 매출',
                    data: dailySalesData,
                    borderColor: '#f05a00',
                    backgroundColor: salesGradient,
                    pointBackgroundColor: '#ffffff',
                    pointBorderColor: '#f05a00',
                    pointBorderWidth: 3,
                    pointRadius: 4,
                    pointHoverRadius: 7,
                    borderWidth: 3,
                    tension: 0.4,
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
                    centerTextPlugin: false,
                    datalabels: {
                        display: false
                    },
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: '#111827',
                        titleColor: '#ffffff',
                        bodyColor: '#ffffff',
                        padding: 12,
                        cornerRadius: 10,
                        callbacks: {
                            label: function (context) {
                                return '매출 ' + formatWon(context.raw);
                            }
                        }
                    }
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
                            callback: function (value) {
                                return Number(value).toLocaleString() + '원';
                            }
                        }
                    }
                }
            }
        });
    }

    const salesTypeCanvas = document.getElementById('salesTypeChart');

    if (salesTypeCanvas) {
        new Chart(salesTypeCanvas, {
            type: 'doughnut',
            data: {
                labels: salesTypeLabels,
                datasets: [{
                    label: '매출',
                    data: salesTypeData,
                    backgroundColor: [
                        '#f97316',
                        '#3b82f6',
                        '#10b981',
                        '#8b5cf6'
                    ],
                    borderColor: '#ffffff',
                    borderWidth: 6,
                    hoverOffset: 12,
                    cutout: '62%'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                layout: {
                    padding: 22
                },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            pointStyle: 'circle',
                            padding: 18,
                            font: {
                                weight: '700'
                            }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#111827',
                        titleColor: '#ffffff',
                        bodyColor: '#ffffff',
                        padding: 12,
                        cornerRadius: 10,
                        callbacks: {
                            label: function (context) {
                                const total = context.dataset.data.reduce(function (sum, item) {
                                    return sum + Number(item || 0);
                                }, 0);

                                const value = Number(context.raw || 0);
                                const rate = total === 0 ? 0 : ((value / total) * 100).toFixed(1);

                                return context.label + ' ' + formatWon(value) + ' (' + rate + '%)';
                            }
                        }
                    },
                    datalabels: {
                        color: '#ffffff',
                        backgroundColor: 'rgba(15, 23, 42, 0.72)',
                        borderRadius: 999,
                        padding: {
                            top: 6,
                            bottom: 6,
                            left: 9,
                            right: 9
                        },
                        font: {
                            weight: '900',
                            size: 13
                        },
                        formatter: function (value, context) {
                            const total = context.chart.data.datasets[0].data.reduce(function (sum, item) {
                                return sum + Number(item || 0);
                            }, 0);

                            if (total === 0) {
                                return '';
                            }

                            const rate = (Number(value || 0) / total) * 100;

                            if (rate < 4) {
                                return '';
                            }

                            return rate.toFixed(1) + '%';
                        }
                    }
                }
            }
        });
    }
</script>

</body>
</html>