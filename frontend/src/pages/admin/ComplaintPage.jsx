import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import * as complaintApi from '../../apis/complaintApi';

const statusOptions = ['', '접수', '확인중', '배정', '처리중', '완료', '반려'];
const typeOptions = ['', '예약문의', '결제문의', '회원문의', '충전기고장', '기기고장', '이용문의'];

const defaultFaultAnswer = `신고해 주신 충전기 이상 내용을 확인했습니다.\n해당 충전기는 장애점검관리로 접수되었으며, 시설관리담당자가 점검 및 수리를 진행할 예정입니다.\n신속히 처리하겠습니다.`;

const getBadgeClass = (value = '') => {
  if (['반려', '취소'].includes(value)) {
    return 'danger';
  }
  if (['처리중', '배정', '고장 의심', '시설장애 의심'].includes(value)) {
    return 'warning';
  }
  if (['완료', '일반민원', '예약문의', '결제문의', '회원문의'].includes(value)) {
    return 'green';
  }
  return 'blue';
};

const AnswerModal = ({ complaint, defaultAnswer, onClose, onSubmit }) => {
  console.log('AnswerModal 렌더링', complaint?.complaintId);

  const [answerContent, setAnswerContent] = useState(defaultAnswer || '');

  const submit = () => {
    console.log('민원 답변 모달 저장', complaint?.complaintId, answerContent);

    if (!answerContent.trim()) {
      alert('사용자에게 전달할 답변을 입력해 주세요.');
      return;
    }

    onSubmit(answerContent.trim());
  };

  return (
    <div className="admin-modal-backdrop">
      <div className="admin-modal complaint-answer-modal">
        <div className="admin-modal-head">
          <div>
            <p>민원 답변</p>
            <h2>{complaint?.title}</h2>
          </div>
          <button type="button" onClick={onClose}>닫기</button>
        </div>

        <div className="complaint-answer-guide">
          <strong>사용자에게 보여질 처리 결과입니다.</strong>
          <p>내 민원 상세 화면에서 이 답변을 확인할 수 있습니다.</p>
        </div>

        <label className="complaint-answer-field">
          답변 내용
          <textarea
            value={answerContent}
            onChange={(e) => setAnswerContent(e.target.value)}
            rows={9}
            placeholder="민원 처리 결과를 입력하세요."
          />
        </label>

        <div className="admin-action-row right">
          <button type="button" onClick={submit}>답변 저장 및 완료</button>
        </div>
      </div>
    </div>
  );
};

const ComplaintPage = () => {
  console.log('Admin ComplaintPage 렌더링');

  const [searchParams] = useSearchParams();
  const [complaints, setComplaints] = useState([]);
  const [selected, setSelected] = useState(null);
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
  const [answerTarget, setAnswerTarget] = useState(null);
  const [answerDefault, setAnswerDefault] = useState('');
  const [loading, setLoading] = useState(false);

  const localSummary = useMemo(() => {
    return {
      total: Number(summary.total || summary.TOTAL || complaints.length || 0),
      pending: Number(summary.pending || summary.PENDING || 0),
      facilityFault: Number(summary.facilityfault || summary.facilityFault || summary.FACILITYFAULT || 0),
      completed: Number(summary.completed || summary.COMPLETED || 0),
    };
  }, [summary, complaints.length]);

  const normalizeListResponse = (data) => {
    console.log('민원 목록 응답 정규화', data);

    if (Array.isArray(data)) {
      return {
        items: data,
        page: 1,
        size: 10,
        totalCount: data.length,
        totalPages: data.length > 10 ? Math.ceil(data.length / 10) : 1,
        summary: {},
      };
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
        size: pageInfo.size,
      };
      const response = await complaintApi.getAdminComplaints(params);
      const normalized = normalizeListResponse(response.data);
      const nextList = Array.isArray(normalized.items) ? normalized.items : [];
      console.log('관리자 민원 목록 정규화 결과', normalized);

      setComplaints(nextList);
      setSummary(normalized.summary || {});
      setPageInfo({
        page: normalized.page,
        size: normalized.size,
        totalCount: normalized.totalCount,
        totalPages: normalized.totalPages,
      });
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
    loadComplaints(1);
  }, []);

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
    setFilter({
      status: '',
      complaintType: '',
      keyword: '',
      stationId: '',
      chargerId: '',
      createdFrom: '',
      createdTo: '',
    });
    setTimeout(() => loadComplaints(1), 0);
  };

  const openAnswer = (complaint, defaultAnswer = '') => {
    console.log('민원 답변 모달 열기', complaint?.complaintId);
    setAnswerTarget(complaint);
    setAnswerDefault(defaultAnswer);
  };

  const submitAnswer = async (answerContent) => {
    console.log('민원 답변 저장', answerTarget?.complaintId, answerContent);

    try {
      await complaintApi.answerComplaint(answerTarget.complaintId, {
        answerContent,
        memo: '민원 답변 완료',
      });
      alert('답변이 저장되고 민원이 완료 처리되었습니다.');
      setAnswerTarget(null);
      setAnswerDefault('');
      await loadComplaints(pageInfo.page);
    } catch (error) {
      console.log('민원 답변 저장 실패', error);
      alert(error.response?.data?.message || '민원 답변 저장 중 오류가 발생했습니다.');
    }
  };

  const registerFault = async (complaint) => {
    console.log('민원 장애접수 클릭', complaint?.complaintId);

    if (!complaint?.stationId || !complaint?.chargerId) {
      alert('충전소와 충전기가 연결된 민원만 장애접수할 수 있습니다.');
      return;
    }

    if (!window.confirm('이 민원을 장애점검관리로 접수하고 사용자 민원은 완료 처리할까요?')) {
      return;
    }

    try {
      const response = await complaintApi.registerComplaintFault(complaint.complaintId, {
        answerContent: defaultFaultAnswer,
        faultType: '충전기고장',
        title: complaint.title,
        description: complaint.content,
      });
      console.log('민원 장애접수 응답', response.data);
      alert(response.data?.message || '장애가 접수되었습니다.');
      await loadComplaints(pageInfo.page);
    } catch (error) {
      console.log('민원 장애접수 실패', error);
      alert(error.response?.data?.message || '장애접수 중 오류가 발생했습니다.');
    }
  };

  const movePage = (nextPage) => {
    console.log('민원 페이지 이동', nextPage);
    if (nextPage < 1 || nextPage > pageInfo.totalPages || nextPage === pageInfo.page) {
      return;
    }
    loadComplaints(nextPage);
  };

  return (
    <section className="admin-page admin-complaint-modern-page">
      <div className="admin-page-header">
        <div>
          <p>민원관리</p>
          <h1>민원 접수·분류·처리</h1>
          <span>AI 분류 결과를 확인하고 일반 문의는 답변, 시설 문제는 장애·점검관리로 이관합니다.</span>
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
        <label>
          상태
          <select name="status" value={filter.status} onChange={changeFilter}>
            {statusOptions.map((status) => <option value={status} key={status || 'all'}>{status || '전체 상태'}</option>)}
          </select>
        </label>
        <label>
          유형
          <select name="complaintType" value={filter.complaintType} onChange={changeFilter}>
            {typeOptions.map((type) => <option value={type} key={type || 'all-type'}>{type || '전체 유형'}</option>)}
          </select>
        </label>
        <label>
          접수 시작일
          <input type="date" name="createdFrom" value={filter.createdFrom} onChange={changeFilter} />
        </label>
        <label>
          접수 종료일
          <input type="date" name="createdTo" value={filter.createdTo} onChange={changeFilter} />
        </label>
        <label className="search-wide">
          검색어
          <input name="keyword" value={filter.keyword} onChange={changeFilter} placeholder="제목, 내용, 회원, 충전소, 충전기, 민원번호 검색" />
        </label>
        <div className="search-button-row">
          <button type="submit" disabled={loading}>{loading ? '조회 중' : '조회'}</button>
          <button type="button" className="line" onClick={resetSearch}>초기화</button>
        </div>
      </form>

      <div className="admin-grid admin-grid-2-1 complaint-work-layout">
        <article className="admin-panel complaint-list-panel">
          <div className="admin-panel-title">
            <div>
              <strong>민원 목록</strong>
              <p>총 {pageInfo.totalCount}건 · {pageInfo.page}/{Math.max(pageInfo.totalPages, 1)}페이지</p>
            </div>
          </div>

          <div className="complaint-card-list">
            {loading && <div className="empty-box">민원 목록을 불러오는 중입니다.</div>}
            {!loading && complaints.length === 0 && <div className="empty-box">조회된 민원이 없습니다.</div>}
            {!loading && complaints.map((item) => (
              <button
                type="button"
                className={`complaint-card ${selected?.complaintId === item.complaintId ? 'active' : ''}`}
                key={item.complaintId}
                onClick={() => {
                  console.log('민원 선택', item.complaintId);
                  setSelected(item);
                }}
              >
                <div className="complaint-card-top">
                  <span className="admin-badge blue">{item.complaintType}</span>
                  <span className={`admin-badge ${getBadgeClass(item.aiLabel)}`}>AI: {item.aiLabel || '일반민원'}</span>
                  <span className={`admin-badge ${getBadgeClass(item.status)}`}>{item.status}</span>
                </div>
                <strong>{item.title}</strong>
                <p>{item.memberName || item.userId || '회원'} · {item.stationName || '-'} · {item.chargerName || '-'}</p>
                <div className="complaint-card-bottom">
                  <span>접수일 {item.createdAtText || item.createdAt || '-'}</span>
                  {item.linkedFaultId && <em>장애 #{item.linkedFaultId}</em>}
                </div>
                <small>{item.aiReason || item.content}</small>
              </button>
            ))}
          </div>

          {pageInfo.totalPages > 1 && (
            <div className="admin-pagination">
              <button type="button" onClick={() => movePage(pageInfo.page - 1)} disabled={pageInfo.page <= 1}>이전</button>
              {Array.from({ length: pageInfo.totalPages }, (_, index) => index + 1).map((pageNo) => (
                <button
                  type="button"
                  key={pageNo}
                  className={pageNo === pageInfo.page ? 'active' : ''}
                  onClick={() => movePage(pageNo)}
                >
                  {pageNo}
                </button>
              ))}
              <button type="button" onClick={() => movePage(pageInfo.page + 1)} disabled={pageInfo.page >= pageInfo.totalPages}>다음</button>
            </div>
          )}
        </article>

        <article className="admin-panel complaint-detail-panel">
          <div className="admin-panel-title"><strong>처리 상세</strong></div>

          {!selected ? (
            <div className="empty-box">민원을 선택해 주세요.</div>
          ) : (
            <div className="admin-detail-box complaint-detail-box">
              <div className="complaint-detail-headline">
                <span className={`admin-badge ${getBadgeClass(selected.status)}`}>{selected.status}</span>
                <h3>{selected.title}</h3>
                <p>민원번호 #{selected.complaintId}</p>
              </div>

              <div className="complaint-ai-box">
                <strong>AI 분류 결과</strong>
                <div>
                  <span className={`admin-badge ${getBadgeClass(selected.aiLabel)}`}>{selected.aiLabel || '일반민원'}</span>
                  <b>신뢰도 {Math.round((selected.aiConfidence || 0.76) * 100)}%</b>
                </div>
                <p>{selected.aiReason || '민원 내용 기반 자동 분류 결과입니다.'}</p>
              </div>

              <dl>
                <div><dt>유형</dt><dd>{selected.complaintType}</dd></div>
                <div><dt>충전소</dt><dd>{selected.stationName || '-'}</dd></div>
                <div><dt>충전기</dt><dd>{selected.chargerName || '-'}</dd></div>
                <div><dt>회원</dt><dd>{selected.memberName || selected.userId || '-'}</dd></div>
                <div><dt>접수일</dt><dd>{selected.createdAtText || selected.createdAt || '-'}</dd></div>
                <div><dt>완료일</dt><dd>{selected.closedAtText || selected.closedAt || '-'}</dd></div>
                {selected.linkedFaultId && <div><dt>연결 장애</dt><dd>#{selected.linkedFaultId} · {selected.linkedFaultStatus}</dd></div>}
              </dl>

              <div className="admin-note-box">
                <strong>민원 내용</strong>
                <p>{selected.content}</p>
              </div>

              <div className="complaint-answer-result">
                <strong>처리 결과</strong>
                <p>{selected.answerContent || selected.adminMemo || '아직 등록된 답변이 없습니다.'}</p>
              </div>

              <div className="admin-action-row right">
                <button type="button" disabled={selected.status === '완료'} onClick={() => registerFault(selected)}>장애접수</button>
                <button type="button" disabled={selected.status === '완료'} onClick={() => openAnswer(selected)}>답변</button>
              </div>
            </div>
          )}
        </article>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title"><strong>민원 처리 흐름</strong></div>
        <div className="admin-flow-row">
          {['민원 접수', 'AI 자동 분류', '운영담당자 확인', '일반 문의 답변', '고장 민원 장애접수'].map((step, index) => (
            <div className="admin-flow-step" key={step}><span>{index + 1}</span><b>{step}</b></div>
          ))}
        </div>
      </div>

      {answerTarget && (
        <AnswerModal
          complaint={answerTarget}
          defaultAnswer={answerDefault}
          onClose={() => setAnswerTarget(null)}
          onSubmit={submitAnswer}
        />
      )}
    </section>
  );
};

export default ComplaintPage;
