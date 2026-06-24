/* =========================================================
   예약 상세 / 충전 시작 시뮬레이션 확인 SQL
========================================================= */

-- 1. 최근 예약 상세 상태 확인
select
    r.reservation_id,
    r.status as reservation_status,
    r.start_time as reserved_start_time,
    r.end_time as reserved_end_time,
    r.verified_at,
    r.auth_code,
    c.charger_id,
    c.charger_name,
    c.status as charger_status,
    cs.station_name
from reservation r
join charger c on r.charger_id = c.charger_id
join charging_station cs on c.station_id = cs.station_id
order by r.reservation_id desc
limit 10;

-- 2. 충전 세션 확인
select
    session_id,
    reservation_id,
    actual_start_time,
    actual_end_time,
    start_soc,
    end_soc,
    actual_kwh,
    actual_minutes,
    actual_cost,
    status
from charging_session
order by session_id desc
limit 10;

-- 3. 충전기 상태 집계 확인
select status, count(*)
from charger
group by status
order by status;
