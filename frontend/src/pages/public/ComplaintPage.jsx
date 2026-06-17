import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import * as complaintApi from "../../apis/complaintApi";
import "../../styles/complaint.css";

const ComplaintPage = () => {
  console.log("ComplaintPage 렌더링");

  const navigate = useNavigate();

  const [form, setForm] = useState({
    title: "",
    complaintType: "충전기고장",
    priority: "NORMAL",
    content: "",
    stationName: "",
    chargerName: "",
    notifyEmail: true,
    notifySms: true,
    notifySite: true,
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  const changeValue = (e) => {
    const { name, value, type, checked } = e.target;

    console.log("민원 입력 변경", name, value);

    setForm({
      ...form,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const submitComplaint = async (e) => {
    e.preventDefault();

    console.log("민원 등록 submit", form);

    if (!form.complaintType) {
      alert("민원 유형을 선택하세요.");
      return;
    }

    if (!form.stationName.trim()) {
      alert("관련 충전소를 입력하세요.");
      return;
    }

    if (!form.title.trim()) {
      alert("민원 제목을 입력하세요.");
      return;
    }

    if (!form.content.trim()) {
      alert("민원 내용을 입력하세요.");
      return;
    }

    try {
      setIsSubmitting(true);

      await complaintApi.createComplaint(form);

      alert("민원이 접수되었습니다.");
      navigate("/complaints/my");
    } catch (error) {
      console.log("민원 등록 실패", error);
      alert("민원 등록 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="customer-complaint-page">
      <aside className="customer-side">
        <h2>고객센터</h2>

        <nav>
          <Link to="/notice">공지사항</Link>
          <Link to="/customer-center">자주 묻는 질문</Link>
          <Link to="/customer-center">1:1 문의</Link>
          <Link to="/complaint" className="active">
            민원 접수
          </Link>
          <Link to="/complaints/my">민원 내역 조회</Link>
        </nav>

        <div className="customer-help-box">
          <h3>고객센터 안내</h3>
          <p>문의사항이 있으신가요?</p>
          <strong>1234-5678</strong>
          <span>평일 09:00 ~ 18:00</span>
          <small>주말/공휴일 휴무</small>
        </div>
      </aside>

      <section className="complaint-form-area">
        <div className="complaint-title-box">
          <h1>민원 접수</h1>
          <p>
            불편하신 사항을 접수해 주세요. 신속하고 정확하게 처리해
            드리겠습니다.
          </p>
        </div>

        <form className="complaint-form" onSubmit={submitComplaint}>
          <div className="complaint-row">
            <label>
              <span className="step">1</span>
              민원 유형 <b>*</b>
            </label>

            <div className="radio-group">
              {[
                "예약문의",
                "결제문의",
                "충전기고장",
                "충전 속도 이상",
                "시설물 파손",
                "서비스 이용 문의",
                "기타",
              ].map((type) => (
                <label key={type}>
                  <input
                    type="radio"
                    name="complaintType"
                    value={type}
                    checked={form.complaintType === type}
                    onChange={changeValue}
                  />
                  {type}
                </label>
              ))}
            </div>
          </div>

          <div className="complaint-row">
            <label>
              <span className="step">2</span>
              관련 충전소 <b>*</b>
            </label>

            <div className="input-with-btn">
              <input
                type="text"
                name="stationName"
                value={form.stationName}
                onChange={changeValue}
                placeholder="예: 부산시청 공영주차장 충전소"
              />

              <button type="button">
                검색
              </button>
            </div>
          </div>

          <div className="complaint-row">
            <label>
              <span className="step">3</span>
              관련 충전기
            </label>

            <select
              name="chargerName"
              value={form.chargerName}
              onChange={changeValue}
            >
              <option value="">충전기를 선택해 주세요.</option>
              <option value="1번 충전기">1번 충전기</option>
              <option value="2번 충전기">2번 충전기</option>
              <option value="3번 충전기">3번 충전기</option>
            </select>
          </div>

          <div className="complaint-row">
            <label>
              <span className="step">4</span>
              제목 <b>*</b>
            </label>

            <div className="input-count-box">
              <input
                type="text"
                name="title"
                value={form.title}
                onChange={changeValue}
                maxLength="100"
                placeholder="예: 2번 충전기가 충전되지 않습니다."
              />

              <span>{form.title.length} / 100</span>
            </div>
          </div>

          <div className="complaint-row align-start">
            <label>
              <span className="step">5</span>
              내용 <b>*</b>
            </label>

            <div className="textarea-count-box">
              <textarea
                name="content"
                value={form.content}
                onChange={changeValue}
                maxLength="2000"
                placeholder="불편 사항을 자세히 입력해 주세요."
              />

              <span>{form.content.length} / 2000</span>
            </div>
          </div>

          <div className="complaint-row">
            <label>
              <span className="step">6</span>
              처리 결과 알림 방법 <b>*</b>
            </label>

            <div className="checkbox-group">
              <label>
                <input
                  type="checkbox"
                  name="notifyEmail"
                  checked={form.notifyEmail}
                  onChange={changeValue}
                />
                이메일
              </label>

              <label>
                <input
                  type="checkbox"
                  name="notifySms"
                  checked={form.notifySms}
                  onChange={changeValue}
                />
                문자(SMS)
              </label>

              <label>
                <input
                  type="checkbox"
                  name="notifySite"
                  checked={form.notifySite}
                  onChange={changeValue}
                />
                사이트 알림
              </label>
            </div>
          </div>

          <div className="complaint-btn-area">
            <button
              type="button"
              className="cancel-btn"
              onClick={() => navigate("/customer-center")}
            >
              취소
            </button>

            <button
              type="submit"
              className="submit-btn"
              disabled={isSubmitting}
            >
              {isSubmitting ? "접수 중..." : "민원 접수하기"}
            </button>
          </div>
        </form>
      </section>

      <aside className="complaint-guide">
        <div className="guide-card">
          <h3>민원 접수 안내</h3>
          <p>정확한 접수를 위해 상세하게 작성해 주세요.</p>
          <p>충전기명이 있으면 처리에 도움이 됩니다.</p>
          <p>접수 결과는 선택하신 방법으로 안내해 드립니다.</p>
        </div>

        <div className="guide-card">
          <h3>민원 처리 절차</h3>

          <ol className="process-list">
            <li>
              <span>1</span>
              민원 접수
            </li>
            <li>
              <span>2</span>
              AI 자동 분류
            </li>
            <li>
              <span>3</span>
              담당자 배정
            </li>
            <li>
              <span>4</span>
              처리 진행
            </li>
            <li>
              <span>5</span>
              처리 완료
            </li>
          </ol>
        </div>
      </aside>
    </main>
  );
};

export default ComplaintPage;