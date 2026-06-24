import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import * as reservationApi from "../../apis/reservationApi";
import "../../styles/station-reservation.css";

const pad = (value) => String(value).padStart(2, "0");

// 오늘 날짜를 yyyy-MM-dd 형식으로 반환한다.
const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const addDays = (dateValue, days) => {
  const date = new Date(`${dateValue}T00:00`);
  date.setDate(date.getDate() + days);
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const toDateTimeLocal = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
const timeOptions = Array.from({ length: 24 * 12 }, (_, index) => {
  const hour = Math.floor(index / 12);
  const minute = (index % 12) * 5;
  return `${pad(hour)}:${pad(minute)}`;
});

const isPastDate = (dateValue) => {
  if (!dateValue) {
    return true;
  }

  return dateValue < today();
};

const isPastTimeOption = (dateValue, timeValue) => {
  if (!dateValue || !timeValue) {
    return true;
  }

  if (dateValue < today()) {
    return true;
  }

  if (dateValue > today()) {
    return false;
  }

  const reservationDateTime = new Date(`${dateValue}T${timeValue}`);
  return reservationDateTime.getTime() <= Date.now();
};

const getNextAvailableTime = (dateValue) => {
  return timeOptions.find((time) => !isPastTimeOption(dateValue, time)) || "";
};

const getInitialReservationDateTime = () => {
  const todayValue = today();
  const nextTime = getNextAvailableTime(todayValue);

  if (nextTime) {
    return {
      reservationDate: todayValue,
      startTimeValue: nextTime,
    };
  }

  return {
    reservationDate: addDays(todayValue, 1),
    startTimeValue: "00:00",
  };
};

const ReservationPage = () => {
  console.log("ReservationPage 렌더링");

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const chargerId = searchParams.get("chargerId");
  const stationId = searchParams.get("stationId");

  const [charger, setCharger] = useState(null);
  const [chargerList, setChargerList] = useState([]);
  const [vehicleList, setVehicleList] = useState([]);
  const [currentHeldChargerId, setCurrentHeldChargerId] = useState("");

  const initialReservationDateTime = useMemo(() => getInitialReservationDateTime(), []);

  const [form, setForm] = useState({
    vehicleId: "",
    chargerId: "",
    stationId: "",
    reservationDate: initialReservationDateTime.reservationDate,
    startTimeValue: initialReservationDateTime.startTimeValue,
    currentSoc: 30,
    targetSoc: 80,
  });

  const selectedVehicle = useMemo(() => {
    return vehicleList.find((vehicle) => String(vehicle.vehicleId) === String(form.vehicleId));
  }, [vehicleList, form.vehicleId]);

  const selectedCharger = useMemo(() => {
    return chargerList.find((item) => String(item.chargerId) === String(form.chargerId));
  }, [chargerList, form.chargerId]);

  const hasSelectableCharger = useMemo(() => {
    return chargerList.some((item) => {
      const status = item.chargerStatus || item.status;
      return status === "사용가능" && !item.selectedByOther;
    });
  }, [chargerList]);

  const isPastReservationTime = useMemo(() => {
    return isPastDate(form.reservationDate) || isPastTimeOption(form.reservationDate, form.startTimeValue);
  }, [form.reservationDate, form.startTimeValue]);

  const summary = useMemo(() => {
    if (!selectedVehicle || !selectedCharger) {
      return null;
    }

    const battery = Number(selectedVehicle.batteryCapacityKwh);
    const speed = Number(selectedCharger.chargingSpeedKw);
    const price = Number(selectedCharger.pricePerKwh);
    const currentSoc = Number(form.currentSoc);
    const targetSoc = Number(form.targetSoc);

    if (!battery || !speed || targetSoc <= currentSoc) {
      return null;
    }

    const requiredKwh = battery * (targetSoc - currentSoc) / 100;
    const estimatedMinutes = Math.ceil((requiredKwh / speed) * 60);
    const estimatedCost = Math.round(requiredKwh * price);
    const startDate = new Date(`${form.reservationDate}T${form.startTimeValue}`);
    const endDate = new Date(startDate.getTime() + estimatedMinutes * 60 * 1000);

    return {
      requiredKwh: requiredKwh.toFixed(2),
      estimatedMinutes,
      estimatedCost,
      startTime: toDateTimeLocal(startDate),
      endTime: toDateTimeLocal(endDate),
      endTimeText: toDateTimeLocal(endDate).replace("T", " "),
    };
  }, [selectedVehicle, selectedCharger, form]);

  useEffect(() => {
    const getFormData = async () => {
      console.log("예약 폼 데이터 조회", { chargerId, stationId });

      if (!chargerId && !stationId) {
        alert("예약할 충전소 정보가 없습니다. 충전소를 다시 선택해 주세요.");
        navigate("/stations");
        return;
      }

      try {
        const response = chargerId
          ? await reservationApi.formData(chargerId)
          : await reservationApi.formDataByStation(stationId);

        console.log("예약 폼 데이터 응답", response.data);

        const data = response.data || {};
        const nextChargerList = data.chargerList || [];
        const holdChargerId = data.holdChargerId ? String(data.holdChargerId) : "";
        const selectedByHold = nextChargerList.find((item) => String(item.chargerId) === holdChargerId);
        const firstSelectable = nextChargerList.find((item) => {
          const status = item.chargerStatus || item.status;
          return status === "사용가능" && !item.selectedByOther;
        });
        const firstCharger = selectedByHold || data.charger || firstSelectable || nextChargerList[0];
        const defaultVehicle = data.vehicleList?.find((vehicle) => vehicle.isDefault) || data.vehicleList?.[0];

        if (!firstCharger) {
          alert(data.message || "등록된 충전기가 없습니다. 다른 충전소를 선택해 주세요.");
          navigate("/stations");
          return;
        }

        setCharger(firstCharger);
        setChargerList(nextChargerList);
        setVehicleList(data.vehicleList || []);
        setCurrentHeldChargerId(holdChargerId);
        setForm((prev) => ({
          ...prev,
          chargerId: holdChargerId || "",
          stationId: String(firstCharger?.stationId || stationId || ""),
          vehicleId: String(defaultVehicle?.vehicleId || ""),
        }));
      } catch (error) {
        console.log("예약 폼 데이터 조회 실패", error);
        alert(error.response?.data?.message || "예약 정보를 불러오지 못했습니다. 충전소를 다시 선택해 주세요.");
        navigate("/stations");
      }
    };

    getFormData();
  }, [chargerId, stationId, navigate]);

  useEffect(() => {
    console.log("예약 날짜/시간 유효성 확인", {
      reservationDate: form.reservationDate,
      startTimeValue: form.startTimeValue,
    });

    if (isPastDate(form.reservationDate)) {
      const nextReservation = getInitialReservationDateTime();
      setForm((prev) => ({
        ...prev,
        reservationDate: nextReservation.reservationDate,
        startTimeValue: nextReservation.startTimeValue,
      }));
      return;
    }

    if (isPastTimeOption(form.reservationDate, form.startTimeValue)) {
      const nextTime = getNextAvailableTime(form.reservationDate);

      if (nextTime) {
        setForm((prev) => ({
          ...prev,
          startTimeValue: nextTime,
        }));
        return;
      }

      setForm((prev) => ({
        ...prev,
        reservationDate: addDays(form.reservationDate, 1),
        startTimeValue: "00:00",
      }));
    }
  }, [form.reservationDate, form.startTimeValue]);

  useEffect(() => {
    if (!summary || !form.stationId || isPastReservationTime) {
      return;
    }

    const getChargerStatus = async () => {
      try {
        const response = await reservationApi.chargerStatus({
          stationId: form.stationId,
          reservationDate: form.reservationDate,
          startTime: form.startTimeValue,
          estimatedMinutes: summary.estimatedMinutes,
        });

        console.log("충전기 상태 응답", response.data);
        if (Array.isArray(response.data)) {
          setChargerList((prev) =>
            prev.map((chargerItem) => {
              const updated = response.data.find((item) => String(item.chargerId) === String(chargerItem.chargerId));
              return updated ? { ...chargerItem, ...updated } : chargerItem;
            })
          );
        }
      } catch (error) {
        console.log("충전기 상태 조회 오류", error);
      }
    };

    getChargerStatus();
  }, [form.stationId, form.reservationDate, form.startTimeValue, summary?.estimatedMinutes, isPastReservationTime]);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("예약 입력 변경", name, value);
    setForm({ ...form, [name]: value });
  };

  const goComplaint = (targetCharger) => {
    console.log("예약 화면 충전기 민원 접수 이동", targetCharger);

    const targetStationId = targetCharger?.stationId || form.stationId || stationId;
    const targetChargerId = targetCharger?.chargerId || form.chargerId;

    const params = new URLSearchParams();
    params.set("type", "충전기고장");

    if (targetStationId) {
      params.set("stationId", targetStationId);
    }

    if (targetChargerId) {
      params.set("chargerId", targetChargerId);
    }

    navigate(`/complaint?${params.toString()}`);
  };

  const changeCharger = async (newChargerId) => {
    console.log("충전기 선택 변경", newChargerId);

    if (String(newChargerId) === String(currentHeldChargerId)) {
      setForm({ ...form, chargerId: String(newChargerId) });
      return;
    }

    try {
      const response = await reservationApi.changeLock(currentHeldChargerId || newChargerId, newChargerId);
      console.log("충전기 선점 변경 응답", response.data);

      if (!response.data?.success) {
        alert(response.data?.message || "다른 사용자가 선택 중인 충전기입니다.");
        return;
      }

      setCurrentHeldChargerId(String(newChargerId));
      setForm({ ...form, chargerId: String(newChargerId) });
    } catch (error) {
      console.log("충전기 선점 변경 오류", error);
      alert("충전기 선택 변경 중 오류가 발생했습니다.");
    }
  };

  const submitReservation = async (e) => {
    e.preventDefault();
    console.log("예약 submit", form, summary);

    if (!form.vehicleId) {
      alert("예약할 차량을 선택해 주세요.");
      return;
    }

    if (!form.chargerId) {
      alert("예약 가능한 충전기를 선택해 주세요.");
      return;
    }

    if (!summary) {
      alert("예약 조건을 확인해 주세요. 목표 배터리 잔량은 현재 잔량보다 높아야 합니다.");
      return;
    }

    if (isPastReservationTime) {
      alert("현재 시간보다 이전 시간으로 예약할 수 없습니다.");
      return;
    }

    try {
      const response = await reservationApi.create({
        vehicleId: form.vehicleId,
        chargerId: form.chargerId,
        reservationDate: form.reservationDate,
        startTime: summary.startTime,
        endTime: summary.endTime,
        currentSoc: form.currentSoc,
        targetSoc: form.targetSoc,
        requiredKwh: summary.requiredKwh,
        estimatedMinutes: summary.estimatedMinutes,
        estimatedCost: summary.estimatedCost,
      });

      console.log("예약 등록 응답", response.data);
      const reservationId = response.data?.reservationId || response.data?.reservation?.reservationId;
      alert(response.data?.message || "예약이 완료되었습니다.");
      navigate(reservationId ? `/reservation/complete?reservationId=${reservationId}` : "/my-reservations");
    } catch (error) {
      console.log("예약 등록 오류", error);
      alert(error.response?.data?.message || "예약 등록 중 오류가 발생했습니다.");
    }
  };

  if (!charger) {
    return <section className="reservation-page"><div className="reservation-inner">예약 정보를 불러오는 중입니다.</div></section>;
  }

  return (
    <section className="reservation-page">
      <div className="reservation-inner">
        <div className="reservation-title">
          <h1>충전 예약</h1>
          <p>차량과 충전기, 배터리 잔량을 기준으로 예상 시간과 비용을 계산합니다.</p>
        </div>

        <div className="reservation-layout">
          <form className="reservation-form-card" onSubmit={submitReservation}>
            <div className="form-section">
              <h3>예약 충전소</h3>
              <div className="station-mini-card">
                <strong>{charger.stationName}</strong>
                <p>{charger.address}</p>
                <button
                  type="button"
                  className="station-complaint-btn"
                  onClick={() => goComplaint(selectedCharger || charger)}
                >
                  이 충전소/충전기 민원 접수
                </button>
              </div>
            </div>

            <div className="form-section">
              <h3>예약 차량</h3>
              <select name="vehicleId" value={form.vehicleId} onChange={changeValue}>
                {vehicleList.map((vehicle) => (
                  <option key={vehicle.vehicleId} value={vehicle.vehicleId}>
                    {vehicle.vehicleNickname || vehicle.modelName} / {vehicle.modelName} / {vehicle.batteryCapacityKwh}kWh
                  </option>
                ))}
              </select>
              {vehicleList.length === 0 && <Link to="/vehicles/register">차량 먼저 등록하기</Link>}
            </div>

            <div className="form-section">
              <h3>충전기 선택</h3>
              <div className="charger-select-list">
                {chargerList.map((item) => {
                  const status = item.chargerStatus || item.status;
                  const disabled = item.selectedByOther || status !== "사용가능";
                  return (
                    <label className={`charger-option ${disabled ? "charger-disabled" : ""}`} key={item.chargerId}>
                      <input
                        type="radio"
                        name="chargerId"
                        value={item.chargerId}
                        checked={String(form.chargerId) === String(item.chargerId)}
                        disabled={disabled}
                        onChange={() => changeCharger(item.chargerId)}
                      />
                      <span>
                        <strong>{item.chargerName}</strong>
                        <em>{item.chargerType} · {item.connectorType} · {item.chargingSpeedKw}kW</em>
                      </span>
                      <b className={disabled ? "charger-status unavailable" : "charger-status available"}>
                        {item.selectedByOther ? "선택중" : status}
                      </b>
                      <button
                        type="button"
                        className="charger-complaint-btn"
                        onClick={(event) => {
                          event.preventDefault();
                          event.stopPropagation();
                          goComplaint(item);
                        }}
                      >
                        이 충전기 민원 접수
                      </button>
                    </label>
                  );
                })}
              </div>
              {!hasSelectableCharger && (
                <p className="reservation-help-message">
                  현재 예약 가능한 충전기가 없습니다. 다른 충전소를 선택하거나 시간을 변경해 주세요.
                </p>
              )}
            </div>

            <div className="form-section">
              <h3>예약 시간</h3>
              <div className="form-grid">
                <div className="form-group">
                  <label>예약 날짜</label>
                  <input
                    type="date"
                    name="reservationDate"
                    min={today()}
                    value={form.reservationDate}
                    onChange={changeValue}
                  />
                </div>
                <div className="form-group">
                  <label>시작 시간</label>
                  <select name="startTimeValue" value={form.startTimeValue} onChange={changeValue}>
                    {timeOptions.map((time) => {
                      const disabled = isPastTimeOption(form.reservationDate, time);
                      return (
                        <option key={time} value={time} disabled={disabled}>
                          {disabled ? `${time} 예약 불가` : time}
                        </option>
                      );
                    })}
                  </select>
                </div>
              </div>
              {isPastReservationTime && (
                <p className="reservation-time-message">
                  현재 시간보다 이전 시간은 예약할 수 없습니다. 예약 가능한 시간으로 다시 선택해 주세요.
                </p>
              )}
            </div>

            <div className="form-section">
              <h3>배터리 잔량</h3>
              <div className="form-grid">
                <div className="form-group">
                  <label>현재 배터리 잔량</label>
                  <input type="number" name="currentSoc" min="0" max="100" value={form.currentSoc} onChange={changeValue} />
                </div>
                <div className="form-group">
                  <label>목표 배터리 잔량</label>
                  <input type="number" name="targetSoc" min="1" max="100" value={form.targetSoc} onChange={changeValue} />
                </div>
              </div>
            </div>

            <button
              type="submit"
              className="submit-btn"
              disabled={vehicleList.length === 0 || !form.chargerId || !summary || isPastReservationTime}
            >
              예약
            </button>
          </form>

          <aside className="reservation-summary-card">
            <div className="summary-header">
              <h2>예약 예상 결과</h2>
              <p>입력한 예약 조건을 기준으로 계산됩니다.</p>
            </div>
            <div className="summary-station">
              <span className="summary-icon">⚡</span>
              <div>
                <strong>{charger.stationName}</strong>
                <p>{selectedCharger ? `${selectedCharger.chargerName} · ${selectedCharger.connectorType}` : "-"}</p>
              </div>
            </div>
            <div className="summary-list">
              <div><span>선택 차량</span><strong>{selectedVehicle?.vehicleNickname || selectedVehicle?.modelName || "-"}</strong></div>
              <div><span>배터리 용량</span><strong>{selectedVehicle ? `${selectedVehicle.batteryCapacityKwh}kWh` : "-"}</strong></div>
              <div><span>현재 → 목표</span><strong>{form.currentSoc}% → {form.targetSoc}%</strong></div>
              <div><span>예상 필요 충전량</span><strong>{summary ? `${summary.requiredKwh}kWh` : "-"}</strong></div>
              <div><span>충전기 출력</span><strong>{selectedCharger ? `${selectedCharger.chargingSpeedKw}kW` : "-"}</strong></div>
              <div><span>예상 충전 시간</span><strong>{summary ? `${summary.estimatedMinutes}분` : "-"}</strong></div>
              <div><span>예약 종료 예정</span><strong>{summary?.endTimeText || "-"}</strong></div>
              <div className="cost-row"><span>예상 충전 비용</span><strong>{summary ? `${summary.estimatedCost.toLocaleString("ko-KR")}원` : "-"}</strong></div>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
};

export default ReservationPage;
