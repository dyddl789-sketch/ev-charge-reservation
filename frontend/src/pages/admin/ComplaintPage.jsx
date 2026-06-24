import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import * as complaintApi from '../../apis/complaintApi';

const statusOptions = ['', '접수', '확인중', '배정', '처리중', '완료', '반려', '취소'];

const ComplaintPage = () => {
  console.log('Admin ComplaintPage 렌더링');

  const [searchParams] = useSearchParams();
  const [complaints, setComplaints] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filter, setFilter] = useState({
    status: '',
    keyword: '',
    stationId: searchParams.get('stationId') || '',
    chargerId: searchParams.get('chargerId') || '',
  });
  const [memo, setMemo] = useState('');
  const [loading, setLoading] = useState(false);

  const summary = useMemo(() => {
    return {
      total: complaints.length,
      pending: complaints.filter((item) => ['접수', '확인중', '배정', '처리중'].includes(item.status)).length,
      fault: complaints.filter((item) => String(item.complaintType || '').includes('고장') || String(item.complaintType || '').includes('파손')).length,
      urgent: complaints.filter((item) => item.priority === 'URGENT' || item.priority === 'HIGH').length,
    };
  }, [complaints]);

  const loadComplaints = async () => {
    console.log('관리자 민원 목록 조회', filter);

    try {
      setLoading(true);
      const params = {
        status: filter.status || undefined,
        keyword: filter.keyword || undefined,
        stationId: filter.stationId || undefined,
        chargerId: filter.chargerId || undefined,
      };
      const response = await complaintApi.getAdminComplaints(params);
      const nextList = Array.isArray(response.data) ? response.data : [];
      console.log('관리자 민원 목록 응답', nextList);
      setComplaints(nextList);
      setSelected((prev) => {
        if (prev) {
          return nextList.find((item) => item.complaintId === prev.complaintId) || nextList[0] || null;
        }
        return nextList[0] || null;
      });
    } catch (error) {
      console.log('관리자 민원 목록 조회 실패', error);
      alert(error.response?.data?.message || '민원 목록을 불러오지 못했습니다.');
      setComplaints([]);
      setSelected(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadComplaints();
  }, []);

  const updateStatus = async (complaintId, status) => {
    console.log('민원 상태 변경', complaintId, status, memo);

    try {
      await complaintApi.updateComplaintStatus(complaintId, {
        status,
        memo: memo || `${status} 처리`,
        adminMemo: memo,
      });
      alert('민원 상태가 변경되었습니다.');
      setMemo('');
      await loadComplaints();
    } catch (error) {
      console.log('민원 상태 변경 실패', error);
      alert(error.response?.data?.message || '민원 상태 변경 중 오류가 발생했습니다.');
    }
  };

  const changeFilter = (e) => {
    const { name, value } = e.target;
    setFilter((prev) => ({ ...prev, [name]: value }));
  };

  const submitSearch = (e) => {
    e.preventDefault();
    loadComplaints();
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>민원관리</p>
          <h1>민원 접수·배정·처리</h1>
          <span>사용자 민원을 확인하고 일반 문의는 처리 완료, 시설 문제는 장애·점검관리로 이관합니다.</span>
        </div>
        <button type="button" onClick={loadComplaints}>새로고침</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 민원</span><strong>{summary.total}건</strong><p>접수 기준</p></article>
        <article className="admin-kpi-card"><span>미처리</span><strong>{summary.pending}건</strong><p>접수/처리중 상태</p></article>
        <article className="admin-kpi-card"><span>시설장애</span><strong>{summary.fault}건</strong><p>고장/파손 유형</p></article>
        <article className="admin-kpi-card"><span>긴급</span><strong>{summary.urgent}건</strong><p>우선 처리 대상</p></article>
      </div>

      <form className="admin-filter-panel" onSubmit={submitSearch}>
        <select name="status" value={filter.status} onChange={changeFilter}>
          <option value="">전체 상태</option>
          {statusOptions.filter(Boolean).map((status) => <option key={status} value={status}>{status}</option>)}
        </select>
        <input name="keyword" value={filter.keyword} onChange={changeFilter} placeholder="제목, 내용, 회원, 충전소 검색" />
        <input name="stationId" value={filter.stationId} onChange={changeFilter} placeholder="충전소 ID" />
        <input name="chargerId" value={filter.chargerId} onChange={changeFilter} placeholder="충전기 ID" />
        <button type="submit" disabled={loading}>{loading ? '조회 중' : '조회'}</button>
      </form>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title"><strong>민원 목록</strong></div>
          <div className="admin-list selectable">
            {complaints.length === 0 && <div className="empty-box">조회된 민원이 없습니다.</div>}
            {complaints.map((item) => (
              <button
                type="button"
                className={`admin-list-row ${selected?.complaintId === item.complaintId ? 'active' : ''}`}
                key={item.complaintId}
                onClick={() => {
                  console.log('민원 선택', item.complaintId);
                  setSelected(item);
                }}
              >
                <div>
                  <b>{item.title}</b>
                  <span>{item.complaintType} · {item.memberName || item.userId || '회원'} · {item.stationName || '-'} / {item.chargerName || '-'}</span>
                </div>
                <em className={`admin-badge ${item.priority === 'URGENT' ? 'danger' : item.priority === 'HIGH' ? 'warning' : 'blue'}`}>{item.status}</em>
              </button>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title"><strong>처리 상세</strong></div>

          {!selected ? (
            <div className="empty-box">민원을 선택해 주세요.</div>
          ) : (
            <div className="admin-detail-box">
              <h3>{selected.title}</h3>
              <p>민원번호 #{selected.complaintId}</p>
              <dl>
                <div><dt>유형</dt><dd>{selected.complaintType}</dd></div>
                <div><dt>우선순위</dt><dd>{selected.priority}</dd></div>
                <div><dt>충전소</dt><dd>{selected.stationName || '-'}</dd></div>
                <div><dt>충전기</dt><dd>{selected.chargerName || '-'}</dd></div>
                <div><dt>회원</dt><dd>{selected.memberName || selected.userId || '-'}</dd></div>
                <div><dt>상태</dt><dd>{selected.status}</dd></div>
              </dl>

              <div className="admin-note-box">
                <p>{selected.content}</p>
              </div>

              <div className="admin-approval-box">
                <strong>처리 메모</strong>
                <textarea value={memo} onChange={(e) => setMemo(e.target.value)} placeholder="처리 내용을 입력하세요." />
              </div>

              <div className="admin-action-row">
                <button type="button" onClick={() => updateStatus(selected.complaintId, '배정')}>담당자 배정</button>
                <button type="button" onClick={() => updateStatus(selected.complaintId, '처리중')}>처리중</button>
                <button type="button" onClick={() => updateStatus(selected.complaintId, '완료')}>완료</button>
                <button type="button" className="danger" onClick={() => updateStatus(selected.complaintId, '반려')}>반려</button>
              </div>
            </div>
          )}
        </article>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title"><strong>민원 처리 흐름</strong></div>
        <div className="admin-flow-row">
          {['민원 접수', 'AI 자동 분류', '운영팀 확인', '일반 문의 완료', '시설 문제 이관'].map((step, index) => (
            <div className="admin-flow-step" key={step}><span>{index + 1}</span><b>{step}</b></div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default ComplaintPage;
