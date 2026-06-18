import { useState } from "react";
import { useNavigate } from "react-router-dom";
import * as complaintApi from "../../apis/complaintApi";

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

            <button type="button">검색</button>
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
            처리 결과 알림 <b>*</b>
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

          <button type="submit" className="submit-btn" disabled={isSubmitting}>
            {isSubmitting ? "접수 중..." : "민원 접수하기"}
          </button>
        </div>
      </form>
    </section>
  );
};

export default ComplaintPage;