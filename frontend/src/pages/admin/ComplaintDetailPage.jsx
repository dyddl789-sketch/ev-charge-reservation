import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as complaintApi from '../../apis/complaintApi';

const defaultFaultAnswer = `신고해 주신 충전기 이상 내용을 확인했습니다.\n해당 충전기는 장애점검관리로 접수되었으며, 시설관리담당자가 점검 및 수리를 진행할 예정입니다.\n신속히 처리하겠습니다.`;

const getBadgeClass = (value = '') => {
  if (['반려', '취소'].includes(value)) return 'danger';
  if (['처리중', '배정', '고장 의심', '시설장애 의심'].includes(value)) return 'warning';
  if (['완료', '일반민원', '예약문의', '결제문의', '회원문의'].includes(value)) return 'green';
  return 'blue';
};

const valueText = (value) => value || '-';

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
          <div><p>민원 답변</p><h2>{complaint?.title}</h2></div>
          <button type="button" onClick={onClose}>닫기</button>
        </div>
        <div className="complaint-answer-guide">
          <strong>사용자에게 보여질 처리 결과입니다.</strong>
          <p>내 민원 상세 화면에서 이 답변을 확인할 수 있습니다.</p>
        </div>
        <label className="complaint-answer-field">
          답변 내용
          <textarea value={answerContent} onChange={(e) => setAnswerContent(e.target.value)} rows={9} placeholder="민원 처리 결과를 입력하세요." />
        </label>
        <div className="admin-action-row right"><button type="button" onClick={submit}>답변 저장 및 완료</button></div>
      </div>
    </div>
  );
};

const ComplaintDetailPage = () => {
  console.log('ComplaintDetailPage 렌더링');

  const { complaintId } = useParams();
  const navigate = useNavigate();

  const [complaint, setComplaint] = useState(null);
  const [loading, setLoading] = useState(false);
  const [answerOpen, setAnswerOpen] = useState(false);

  const loadComplaint = async () => {
    console.log('민원 상세 조회', complaintId);
    try {
      setLoading(true);
      const response = await complaintApi.getAdminComplaintDetail(complaintId);
      console.log('민원 상세 응답', response.data);
      setComplaint(response.data || null);
    } catch (error) {
      console.log('민원 상세 조회 실패', error);
      alert(error.response?.data?.message || '민원 상세를 불러오지 못했습니다.');
      setComplaint(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadComplaint();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [complaintId]);

  const submitAnswer = async (answerContent) => {
    console.log('민원 상세 답변 저장', complaint?.complaintId, answerContent);
    try {
      await complaintApi.answerComplaint(complaint.complaintId, { answerContent, memo: '민원 답변 완료' });
      alert('답변이 저장되고 민원이 완료 처리되었습니다.');
      setAnswerOpen(false);
      await loadComplaint();
    } catch (error) {
      console.log('민원 답변 저장 실패', error);
      alert(error.response?.data?.message || '민원 답변 저장 중 오류가 발생했습니다.');
    }
  };

  const registerFault = async () => {
    console.log('민원 상세 장애접수 클릭', complaint?.complaintId);
    if (!complaint?.stationId || !complaint?.chargerId) {
      alert('충전소와 충전기가 연결된 민원만 장애접수할 수 있습니다.');
      return;
    }
    if (!window.confirm('이 민원을 장애점검관리로 접수하고 사용자 민원은 완료 처리할까요?')) return;

    try {
      const response = await complaintApi.registerComplaintFault(complaint.complaintId, {
        answerContent: defaultFaultAnswer,
        faultType: '충전기고장',
        title: complaint.title,
        description: complaint.content,
      });
      console.log('민원 장애접수 응답', response.data);
      alert(response.data?.message || '장애가 접수되었습니다.');
      await loadComplaint();
    } catch (error) {
      console.log('민원 장애접수 실패', error);
      alert(error.response?.data?.message || '장애접수 중 오류가 발생했습니다.');
    }
  };

  if (loading) {
    return <section className="admin-page"><div className="admin-panel"><p className="admin-empty-text">민원 상세를 불러오는 중입니다.</p></div></section>;
  }

  if (!complaint) {
    return (
      <section className="admin-page">
        <div className="admin-panel">
          <p className="admin-empty-text">민원 정보를 찾을 수 없습니다.</p>
          <button type="button" onClick={() => navigate('/admin/complaints')}>목록으로</button>
        </div>
      </section>
    );
  }

  const disabledAction = complaint.status === '완료';
  const historyList = complaint.historyList || [];

  return (
    <section className="admin-page admin-complaint-detail-page">
      <div className="admin-page-header">
        <div>
          <p>민원관리</p>
          <h1>민원 상세보기</h1>
          <span>민원 원문, AI 분류, 처리 결과와 이력을 실제 업무 흐름에 맞게 확인합니다.</span>
        </div>
        <div className="admin-action-row">
          <button type="button" className="line" onClick={() => navigate('/admin/complaints')}>목록으로</button>
          {!disabledAction && <button type="button" onClick={registerFault}>장애접수</button>}
          {!disabledAction && <button type="button" onClick={() => setAnswerOpen(true)}>답변</button>}
        </div>
      </div>

      <div className="admin-detail-hero complaint-detail-hero">
        <div>
          <span className="admin-detail-label">민원번호 #{complaint.complaintId}</span>
          <h2>{complaint.title}</h2>
          <p>{valueText(complaint.memberName || complaint.userId)} · {valueText(complaint.stationName)} · {valueText(complaint.chargerName)}</p>
        </div>
        <em className={`admin-badge ${getBadgeClass(complaint.status)}`}>{complaint.status}</em>
      </div>

      <div className="complaint-detail-workspace">
        <article className="complaint-main-panel">
          <div className="complaint-body-card">
            <strong>민원 내용</strong>
            <p>{complaint.content || '민원 내용이 없습니다.'}</p>
          </div>

          <div className="admin-panel-title compact-title"><div><strong>접수 기본정보</strong><p>사용자와 연결된 충전 인프라 정보를 확인합니다.</p></div></div>
          <div className="complaint-info-list">
            <div><span>유형</span><b>{valueText(complaint.complaintType)}</b></div>
            <div><span>회원</span><b>{valueText(complaint.memberName || complaint.userId)}</b></div>
            <div><span>이메일</span><b>{valueText(complaint.email)}</b></div>
            <div><span>연락처</span><b>{valueText(complaint.phone)}</b></div>
            <div><span>충전소</span><b>{valueText(complaint.stationName)}</b></div>
            <div><span>충전기</span><b>{valueText(complaint.chargerName)}</b></div>
            <div><span>접수일</span><b>{valueText(complaint.createdAtText || complaint.createdAt)}</b></div>
            <div><span>완료일</span><b>{valueText(complaint.closedAtText || complaint.closedAt)}</b></div>
          </div>
        </article>

        <aside className="complaint-side-panel">
          <section className="complaint-side-section">
            <strong>AI 분류 결과</strong>
            <div className="complaint-card-top">
              <span className={`admin-badge ${getBadgeClass(complaint.aiLabel)}`}>{complaint.aiLabel || '일반민원'}</span>
              <span className="admin-badge blue">신뢰도 {Math.round((complaint.aiConfidence || 0.76) * 100)}%</span>
            </div>
            <p>{complaint.aiReason || '민원 내용 기반 자동 분류 결과입니다.'}</p>
          </section>

          {complaint.linkedFaultId && (
            <section className="complaint-side-section green">
              <strong>연결 장애</strong>
              <p>장애 #{complaint.linkedFaultId} · {valueText(complaint.linkedFaultStatus)}</p>
            </section>
          )}

          <section className={`complaint-side-section ${complaint.answerContent ? 'green' : 'orange'}`}>
            <strong>처리 결과</strong>
            <p>{complaint.answerContent || complaint.adminMemo || '아직 등록된 처리 결과가 없습니다.'}</p>
          </section>
        </aside>
      </div>

      <article className="complaint-action-panel">
        <div className="admin-panel-title"><div><strong>처리 액션</strong><p>완료되지 않은 민원은 답변하거나 장애점검관리로 접수할 수 있습니다.</p></div></div>
        {disabledAction ? (
          <div className="complaint-side-section green"><strong>처리 완료</strong><p>{complaint.answerContent || '민원이 완료 처리되었습니다.'}</p></div>
        ) : (
          <div className="admin-action-row right">
            <button type="button" onClick={registerFault}>장애접수</button>
            <button type="button" onClick={() => setAnswerOpen(true)}>답변 작성</button>
          </div>
        )}
      </article>

      <article className="complaint-history-panel">
        <div className="admin-panel-title"><div><strong>처리 이력</strong><p>민원 상태 변경, 답변, 장애접수 기록입니다.</p></div></div>
        <div className="complaint-timeline-list">
          {historyList.map((history) => (
            <div className="complaint-timeline-item" key={history.historyId}>
              <time>{history.createdAtText || history.createdAt || '-'}</time>
              <strong>{history.actionType}</strong>
              <p>{history.memo || `${history.beforeStatus || '-'} → ${history.afterStatus || '-'}`}</p>
            </div>
          ))}
          {historyList.length === 0 && <div className="empty-box">처리 이력이 없습니다.</div>}
        </div>
      </article>

      {answerOpen && (
        <AnswerModal complaint={complaint} defaultAnswer={complaint.answerContent || ''} onClose={() => setAnswerOpen(false)} onSubmit={submitAnswer} />
      )}
    </section>
  );
};

export default ComplaintDetailPage;
