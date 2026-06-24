/* =========================================================
   AI 고도화 적용 후 확인용 SQL
   - 새 테이블 생성은 필요 없습니다.
   - 기존 saved_location, vehicle, vehicle_model, charging_station,
     charger, reservation, notice, faq, complaint 테이블을 사용합니다.
========================================================= */

-- 1. 로그인 사용자의 기본 출발지 확인
select
      member_id
    , location_id
    , location_name
    , address
    , latitude
    , longitude
    , is_default
from saved_location
where is_default = true
order by member_id, location_id;

-- 2. 대표 차량 확인
select
      v.member_id
    , v.vehicle_id
    , v.vehicle_nickname
    , v.is_default
    , vm.manufacturer
    , vm.model_name
    , vm.connector_type
    , vm.max_charging_speed_kw
from vehicle v
join vehicle_model vm
  on v.model_id = vm.model_id
where v.is_default = true
  and coalesce(v.is_deleted, false) = false
order by v.member_id;

-- 3. 사용 가능한 충전기 확인
select
      cs.station_id
    , cs.station_name
    , cs.address
    , c.charger_id
    , c.charger_name
    , c.connector_type
    , c.charging_speed_kw
    , c.price_per_kwh
    , c.status
from charging_station cs
join charger c
  on cs.station_id = c.station_id
where cs.station_status = '운영중'
  and c.status = '사용가능'
order by cs.station_id, c.charger_id;

-- 4. RAG 데이터 확인
select notice_id, title, status, is_public, created_at
from notice
order by created_at desc
limit 5;

select faq_id, category, question, is_active, display_order
from faq
order by display_order asc
limit 5;

select complaint_id, member_id, title, complaint_type, status, created_at
from complaint
order by created_at desc
limit 5;
