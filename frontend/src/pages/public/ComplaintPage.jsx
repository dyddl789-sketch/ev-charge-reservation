import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import * as complaintApi from '../../apis/complaintApi';
import * as stationApi from '../../apis/stationApi';

const getStatusOrder = (status) => {
  if (status === '고장') return 1;
  if (status === '점검중') return 2;
  if (status === '사용가능') return 3;
  if (status === '예약중') return 4;
  if (status === '사용중') return 5;
  return 9;
};

const ComplaintPage = () => {
  console.log('ComplaintPage 렌더링');

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [stationList, setStationList] = useState([]);
  const [chargerList, setChargerList] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingStation, setIsLoadingStation] = useState(false);

  const initialStationId = searchParams.get('stationId') || '';
  const initialChargerId = searchParams.get('chargerId') || '';
  const initialType = searchParams.get('type') || '충전기고장';

  const [form, setForm] = useState({
    title: '',
    complaintType: initialType,
    priority: initialType.includes('고장') ? 'HIGH' : 'NORMAL',
    content: '',
    stationId: initialStationId,
    chargerId: initialChargerId,
    stationName: '',
    chargerName: '',
    notifyEmail: true,
    notifySms: true,
    notifySite: true,
  });

  const sortedChargerList = useMemo(() => {
    return [...chargerList].sort((a, b) => getStatusOrder(a.status) - getStatusOrder(b.status));
  }, [chargerList]);

  useEffect(() => {
    const loadStations = async () => {
      console.log('민원 접수용 충전소 목록 조회');

      try {
        setIsLoadingStation(true);
        const response = await stationApi.list('');
        console.log('민원 접수용 충전소 목록 응답', response.data);
        setStationList(response.data || []);
      } catch (error) {
        console.log('민원 접수용 충전소 목록 조회 실패', error);
      } finally {
        setIsLoadingStation(false);
      }
    };

    loadStations();
  }, []);

  useEffect(() => {
    if (!form.stationId || stationList.length === 0) {
      return;
    }

    const selectedStation = stationList.find((station) => String(station.stationId) === String(form.stationId));

    if (selectedStation) {
      setForm((prev) => ({
        ...prev,
        stationName: selectedStation.stationName,
      }));
    }
  }, [form.stationId, stationList]);

  useEffect(() => {
    const loadChargers = async () => {
      if (!form.stationId) {
        setChargerList([]);
        return;
      }

      console.log('민원 접수용 충전기 목록 조회', form.stationId);

      try {
        const response = await stationApi.chargerList(form.stationId);
        console.log('민원 접수용 충전기 목록 응답', response.data);
        const nextChargerList = response.data || [];
        setChargerList(nextChargerList);

        if (form.chargerId) {
          const selectedCharger = nextChargerList.find((charger) => String(charger.chargerId) === String(form.chargerId));
          if (selectedCharger) {
            setForm((prev) => ({
              ...prev,
              chargerName: selectedCharger.chargerName,
            }));
          }
        }
      } catch (error) {
        console.log('민원 접수용 충전기 목록 조회 실패', error);
      }
    };

    loadChargers();
  }, [form.stationId]);

  const changeValue = (e) => {
    const { name, value, type, checked } = e.target;
    console.log('민원 입력 변경', name, value);

    if (name === 'stationId') {
      const selectedStation = stationList.find((station) => String(station.stationId) === String(value));
      setForm({
        ...form,
        stationId: value,
        chargerId: '',
        chargerName: '',
        stationName: selectedStation?.stationName || '',
      });
      return;
    }

    if (name === 'chargerId') {
      const selectedCharger = chargerList.find((charger) => String(charger.chargerId) === String(value));
      setForm({
        ...form,
        chargerId: value,
        chargerName: selectedCharger?.chargerName || '',
      });
      return;
    }

    if (name === 'complaintType') {
      setForm({
        ...form,
        complaintType: value,
        priority: value.includes('고장') || value.includes('파손') ? 'HIGH' : form.priority,
      });
      return;
    }

    setForm({
      ...form,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const submitComplaint = async (e) => {
    e.preventDefault();
    console.log('민원 등록 submit', form);

    if (!form.complaintType) {
      alert('민원 유형을 선택하세요.');
      return;
    }

    if (!form.stationId) {
      alert('관련 충전소를 선택하세요.');
      return;
    }

    if ((form.complaintType.includes('고장') || form.complaintType.includes('파손')) && !form.chargerId) {
      alert('충전기 장애 민원은 관련 충전기를 선택해 주세요.');
      return;
    }

    if (!form.title.trim()) {
      alert('민원 제목을 입력하세요.');
      return;
    }

    if (!form.content.trim()) {
      alert('민원 내용을 입력하세요.');
      return;
    }

    try {
      setIsSubmitting(true);
      await complaintApi.createComplaint({
        ...form,
        stationId: form.stationId ? Number(form.stationId) : null,
        chargerId: form.chargerId ? Number(form.chargerId) : null,
      });

      alert('민원이 접수되었습니다.');
      navigate('/complaints/my');
    } catch (error) {
      console.log('민원 등록 실패', error);
      alert(error.response?.data?.message || '민원 등록 중 오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="complaint-form-area">
      <div className="complaint-title-box">
        <h1>민원 접수</h1>
        <p>충전소와 충전기를 선택하면 운영기관 담당자가 더 빠르게 확인할 수 있습니다.</p>
      </div>

      <form className="complaint-form" onSubmit={submitComplaint}>
        <div className="complaint-row">
          <label><span className="step">1</span>민원 유형 <b>*</b></label>

          <div className="radio-group">
            {['예약문의', '결제문의', '충전기고장', '충전 속도 이상', '시설물 파손', '서비스 이용 문의', '기타'].map((type) => (
              <label key={type}>
                <input type="radio" name="complaintType" value={type} checked={form.complaintType === type} onChange={changeValue} />
                {type}
              </label>
            ))}
          </div>
        </div>

        <div className="complaint-row">
          <label><span className="step">2</span>관련 충전소 <b>*</b></label>
          <select name="stationId" value={form.stationId} onChange={changeValue} disabled={isLoadingStation}>
            <option value="">충전소를 선택해 주세요.</option>
            {stationList.map((station) => (
              <option key={station.stationId} value={station.stationId}>
                {station.stationName} / {station.address}
              </option>
            ))}
          </select>
        </div>

        <div className="complaint-row">
          <label><span className="step">3</span>관련 충전기</label>
          <select name="chargerId" value={form.chargerId} onChange={changeValue} disabled={!form.stationId}>
            <option value="">충전기를 선택해 주세요.</option>
            {sortedChargerList.map((charger) => (
              <option key={charger.chargerId} value={charger.chargerId}>
                [{charger.status}] {charger.chargerName} / {charger.connectorType} / {charger.chargingSpeedKw}kW
              </option>
            ))}
          </select>
        </div>

        {form.stationName && (
          <div className="complaint-selected-box">
            <strong>선택된 대상</strong>
            <p>{form.stationName}{form.chargerName ? ` / ${form.chargerName}` : ''}</p>
          </div>
        )}

        <div className="complaint-row">
          <label><span className="step">4</span>제목 <b>*</b></label>
          <div className="input-count-box">
            <input type="text" name="title" value={form.title} onChange={changeValue} maxLength="100" placeholder="예: 2번 충전기가 충전되지 않습니다." />
            <span>{form.title.length} / 100</span>
          </div>
        </div>

        <div className="complaint-row align-start">
          <label><span className="step">5</span>내용 <b>*</b></label>
          <div className="textarea-count-box">
            <textarea name="content" value={form.content} onChange={changeValue} maxLength="2000" placeholder="불편 사항을 자세히 입력해 주세요." />
            <span>{form.content.length} / 2000</span>
          </div>
        </div>

        <div className="complaint-row">
          <label><span className="step">6</span>처리 결과 알림 <b>*</b></label>
          <div className="checkbox-group">
            <label><input type="checkbox" name="notifyEmail" checked={form.notifyEmail} onChange={changeValue} />이메일</label>
            <label><input type="checkbox" name="notifySms" checked={form.notifySms} onChange={changeValue} />문자(SMS)</label>
            <label><input type="checkbox" name="notifySite" checked={form.notifySite} onChange={changeValue} />사이트 알림</label>
          </div>
        </div>

        <div className="complaint-btn-area">
          <button type="button" className="cancel-btn" onClick={() => navigate('/customer-center')}>취소</button>
          <button type="submit" className="submit-btn" disabled={isSubmitting}>{isSubmitting ? '접수 중...' : '민원 접수하기'}</button>
        </div>
      </form>
    </section>
  );
};

export default ComplaintPage;
