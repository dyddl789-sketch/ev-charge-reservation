import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import * as complaintApi from '../../apis/complaintApi';
import useAdminPolling from '../../hooks/useAdminPolling';

const statusOptions = ['', '접수', '확인중', '배정', '처리중', '완료', '반려'];
const typeOptions = ['', '예약문의', '결제문의', '회원문의', '충전기고장', '기기고장', '이용문의'];

const getBadgeClass = (value = '') => {
  if (['반려', '취소'].includes(value)) return 'danger';
  if (['처리중', '배정', '고장 의심', '시설장애 의심'].includes(value)) return 'warning';
  if (['완료', '일반민원', '예약문의', '결제문의', '회원문의'].includes(value)) return 'green';
  return 'blue';
};

const ComplaintPage = () => {
  console.log('Admin ComplaintPage 렌더링');

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [complaints, setComplaints] = useState([]);
  const [filter, setFilter] = useState({
    status: '',
    complaintType: '',
    keyword: '',
    stationId: searchParams.get('stationId') || '',
    chargerId: searchParams.get('chargerId') || '',
    createdFrom: '',
    createdTo: '',
  });
  const [pageInfo, setPageInfo] = useState({ page: 1, size: 10, totalCount: 0, totalPages: 0 });
  const [summary, setSummary] = useState({ total: 0, pending: 0, facilityFault: 0, completed: 0 });
  const [loading, setLoading] = useState(false);
  const isFirstFilterChangeRef = useRef(true);
  const filterKey = useMemo(() => JSON.stringify(filter), [filter]);

  const localSummary = useMemo(() => ({
    total: Number(summary.total || summary.TOTAL || complaints.length || 0),
    pending: Number(summary.pending || summary.PENDING || 0),
    facilityFault: Number(summary.facilityfault || summary.facilityFault || summary.FACILITYFAULT || 0),
    completed: Number(summary.completed || summary.COMPLETED || 0),
  }), [summary, complaints.length]);

  const normalizeListResponse = (data) => {
    console.log('민원 목록 응답 정규화', data);
    if (Array.isArray(data)) {
      return { items: data, page: 1, size: 10, totalCount: data.length, totalPages: Math.max(1, Math.ceil(data.length / 10)), summary: {} };
    }
    return {
      items: data?.items || data?.list || [],
      page: data?.page || 1,
      size: data?.size || 10,
      totalCount: data?.totalCount || 0,
      totalPages: data?.totalPages || 0,
      summary: data?.summary || {},
    };
  };

  const loadComplaints = async (nextPage = pageInfo.page) => {
    console.log('관리자 민원 목록 조회', filter, nextPage);
    try {
      setLoading(true);
      const params = {
        status: filter.status || undefined,
        complaintType: filter.complaintType || undefined,
        keyword: filter.keyword || undefined,
        stationId: filter.stationId || undefined,
        chargerId: filter.chargerId || undefined,
        createdFrom: filter.createdFrom || undefined,
        createdTo: filter.createdTo || undefined,
        page: nextPage,
        size: 10,
      };
      const response = await complaintApi.getAdminComplaints(params);
      const normalized = normalizeListResponse(response.data);
      const nextList = Array.isArray(normalized.items) ? normalized.items : [];

      setComplaints(nextList);
      setSummary(normalized.summary || {});
      setPageInfo({ page: normalized.page, size: normalized.size, totalCount: normalized.totalCount, totalPages: normalized.totalPages });
    } catch (error) {
      console.log('관리자 민원 목록 조회 실패', error);
      alert(error.response?.data?.message || '민원 목록을 불러오지 못했습니다.');
      setComplaints([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadComplaints(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (isFirstFilterChangeRef.current) {
      isFirstFilterChangeRef.current = false;
      return undefined;
    }

    const timer = setTimeout(() => {
      console.log('민원 필터 변경 즉시 재조회', filter);
      loadComplaints(1);
    }, 400);

    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterKey]);

  useAdminPolling(() => loadComplaints(pageInfo.page), { label: '민원관리' });

  const changeFilter = (e) => {
    const { name, value } = e.target;
    console.log('민원 필터 변경', name, value);
    setFilter((prev) => ({ ...prev, [name]: value }));
  };

  const submitSearch = (e) => {
    e.preventDefault();
    loadComplaints(1);
  };

  const resetSearch = () => {
    console.log('민원 검색 초기화');
    setFilter({ status: '', complaintType: '', keyword: '', stationId: '', chargerId: '', createdFrom: '', createdTo: '' });
    setTimeout(() => loadComplaints(1), 0);
  };

  const movePage = (nextPage) => {
    console.log('민원 페이지 이동', nextPage);
    if (nextPage < 1 || nextPage > pageInfo.totalPages || nextPage === pageInfo.page) return;
    loadComplaints(nextPage);
  };

  const openDetail = (complaintId) => {
    console.log('민원 상세 이동', complaintId);
    navigate(`/admin/complaints/${complaintId}`);
  };

  return (
    <section className="admin-page admin-complaint-modern-page">
      <div className="admin-page-header">
        <div>
          <p>민원관리</p>
          <h1>민원 접수·분류·처리</h1>
          <span>목록에서 민원을 확인하고 상세 페이지에서 답변 또는 장애접수를 처리합니다.</span>
        </div>
        <button type="button" onClick={() => loadComplaints(pageInfo.page)}>새로고침</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 민원</span><strong>{localSummary.total}건</strong><p>접수 기준</p></article>
        <article className="admin-kpi-card"><span>미처리</span><strong>{localSummary.pending}건</strong><p>접수/처리중 상태</p></article>
        <article className="admin-kpi-card"><span>시설장애</span><strong>{localSummary.facilityFault}건</strong><p>AI 고장 의심 포함</p></article>
        <article className="admin-kpi-card"><span>완료</span><strong>{localSummary.completed}건</strong><p>답변/장애접수 완료</p></article>
      </div>

      <form className="admin-search-panel complaint-search-panel" onSubmit={submitSearch}>
        <label>상태<select name="status" value={filter.status} onChange={changeFilter}>{statusOptions.map((status) => <option value={status} key={status || 'all'}>{status || '전체 상태'}</option>)}</select></label>
        <label>유형<select name="complaintType" value={filter.complaintType} onChange={changeFilter}>{typeOptions.map((type) => <option value={type} key={type || 'all-type'}>{type || '전체 유형'}</option>)}</select></label>
        <label>접수 시작일<input type="date" name="createdFrom" value={filter.createdFrom} onChange={changeFilter} /></label>
        <label>접수 종료일<input type="date" name="createdTo" value={filter.createdTo} onChange={changeFilter} /></label>
        <label className="search-wide">검색어<input name="keyword" value={filter.keyword} onChange={changeFilter} placeholder="제목, 내용, 회원, 충전소, 충전기, 민원번호 검색" /></label>
        <div className="search-button-row">
          <button type="submit" disabled={loading}>{loading ? '조회 중' : '조회'}</button>
          <button type="button" className="line" onClick={resetSearch}>초기화</button>
        </div>
      </form>

      <article className="admin-panel complaint-list-panel full-complaint-list-panel">
        <div className="admin-panel-title">
          <div>
            <strong>민원 목록</strong>
            <p>표형 10개 단위 페이지네이션 · 총 {pageInfo.totalCount}건 · {pageInfo.page}/{Math.max(pageInfo.totalPages, 1)}페이지</p>
          </div>
        </div>

        <div className="complaint-table-wrap">
          {loading && <div className="empty-box">민원 목록을 불러오는 중입니다.</div>}
          {!loading && complaints.length === 0 && <div className="empty-box">조회된 민원이 없습니다.</div>}
          {!loading && complaints.length > 0 && (
            <table className="admin-table complaint-work-table">
              <thead>
                <tr>
                  <th>민원번호</th>
                  <th>유형</th>
                  <th>민원 제목</th>
                  <th>회원</th>
                  <th>충전 인프라</th>
                  <th>AI 분류</th>
                  <th>상태</th>
                  <th>접수일</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {complaints.map((item) => (
                  <tr key={item.complaintId}>
                    <td className="complaint-no-cell">#{item.complaintId}</td>
                    <td><span className="admin-badge blue">{item.complaintType || '-'}</span></td>
                    <td className="complaint-title-cell">
                      <strong>{item.title || '-'}</strong>
                      <p>{item.aiReason || item.content || '민원 내용 요약이 없습니다.'}</p>
                      {item.linkedFaultId && <em className="admin-badge warning">장애 #{item.linkedFaultId}</em>}
                    </td>
                    <td>{item.memberName || item.userId || '-'}</td>
                    <td className="complaint-infra-cell">
                      <b>{item.stationName || '-'}</b>
                      <span>{item.chargerName || '-'}</span>
                    </td>
                    <td><span className={`admin-badge ${getBadgeClass(item.aiLabel)}`}>{item.aiLabel || '일반민원'}</span></td>
                    <td><span className={`admin-badge ${getBadgeClass(item.status)}`}>{item.status || '-'}</span></td>
                    <td>{item.createdAtText || item.createdAt || '-'}</td>
                    <td><button type="button" className="table-action-button" onClick={() => openDetail(item.complaintId)}>상세보기</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {pageInfo.totalPages > 1 && (
          <div className="admin-pagination">
            <button type="button" onClick={() => movePage(pageInfo.page - 1)} disabled={pageInfo.page <= 1}>이전</button>
            {Array.from({ length: pageInfo.totalPages }, (_, index) => index + 1).map((pageNo) => (
              <button type="button" key={pageNo} className={pageNo === pageInfo.page ? 'active' : ''} onClick={() => movePage(pageNo)}>{pageNo}</button>
            ))}
            <button type="button" onClick={() => movePage(pageInfo.page + 1)} disabled={pageInfo.page >= pageInfo.totalPages}>다음</button>
          </div>
        )}
      </article>

      <div className="admin-panel">
        <div className="admin-panel-title"><strong>민원 처리 흐름</strong></div>
        <div className="admin-flow-row">
          {['민원 접수', 'AI 자동 분류', '상세 확인', '일반 문의 답변', '고장 민원 장애접수'].map((step, index) => (
            <div className="admin-flow-step" key={step}><span>{index + 1}</span><b>{step}</b></div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default ComplaintPage;
