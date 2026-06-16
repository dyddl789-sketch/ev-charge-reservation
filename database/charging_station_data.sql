-- =========================================================
-- 부산 전역 테스트용 충전소 10개 + 충전기 30개
-- 목적:
-- - 부산 구석구석에 충전소 마커가 보이도록 테스트 데이터 재구성
-- - 모든 충전기 status = '사용가능'
-- - 충전소별 image_url 적용
-- =========================================================

-- 1. 충전소 이미지 컬럼 추가
alter table charging_station
add column if not exists image_url varchar(255)
default '/images/station/station-default.jpg';

-- 2. 기존 충전소 / 충전기 초기화
-- 주의: reservation, charging_session, favorite_station 등이 연결되어 있으면 cascade로 같이 정리될 수 있음
truncate table charger, charging_station restart identity cascade;

-- 3. 부산 전역 충전소 10개 추가
-- ST_MakePoint(경도, 위도) 순서 주의
insert into charging_station (
      station_name
    , address
    , latitude
    , longitude
    , location
    , operator_name
    , open_time
    , close_time
    , station_status
    , image_url
)
values
(
      '테스트 기장 충전소'
    , '부산광역시 기장군 기장읍 기장대로 560'
    , 35.2446
    , 129.2220
    , ST_SetSRID(ST_MakePoint(129.2220, 35.2446), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-solar-canopy.jpg'
),
(
      '테스트 해운대 충전소'
    , '부산광역시 해운대구 해운대해변로 264'
    , 35.1587
    , 129.1604
    , ST_SetSRID(ST_MakePoint(129.1604, 35.1587), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-red-fast.jpg'
),
(
      '테스트 광안리 충전소'
    , '부산광역시 수영구 광안해변로 219'
    , 35.1532
    , 129.1186
    , ST_SetSRID(ST_MakePoint(129.1186, 35.1532), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-car-charging.jpg'
),
(
      '테스트 남포 충전소'
    , '부산광역시 중구 광복로 72'
    , 35.0983
    , 129.0345
    , ST_SetSRID(ST_MakePoint(129.0345, 35.0983), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-green-park.jpg'
),
(
      '테스트 부산역 충전소'
    , '부산광역시 동구 중앙대로 206'
    , 35.1151
    , 129.0415
    , ST_SetSRID(ST_MakePoint(129.0415, 35.1151), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-fast-row.jpg'
),
(
      '테스트 서면 충전소'
    , '부산광역시 부산진구 중앙대로 730'
    , 35.1578
    , 129.0592
    , ST_SetSRID(ST_MakePoint(129.0592, 35.1578), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-building-charge.jpg'
),
(
      '테스트 사상 충전소'
    , '부산광역시 사상구 광장로 83'
    , 35.1628
    , 128.9846
    , ST_SetSRID(ST_MakePoint(128.9846, 35.1628), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-open-parking.jpg'
),
(
      '테스트 덕천 충전소'
    , '부산광역시 북구 금곡대로 14'
    , 35.2101
    , 129.0080
    , ST_SetSRID(ST_MakePoint(129.0080, 35.2101), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-blue-charger-row.jpg'
),
(
      '테스트 명지 충전소'
    , '부산광역시 강서구 명지국제7로 37'
    , 35.0955
    , 128.9035
    , ST_SetSRID(ST_MakePoint(128.9035, 35.0955), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-wall-parking.jpg'
),
(
      '테스트 다대포 충전소'
    , '부산광역시 사하구 다대로 680'
    , 35.0472
    , 128.9684
    , ST_SetSRID(ST_MakePoint(128.9684, 35.0472), 4326)::geography
    , 'EV Charge'
    , '00:00'
    , '23:59'
    , '운영중'
    , '/images/station/station-300kw-fast.jpg'
);

-- 4. 각 충전소별 충전기 3개씩 추가
-- 모두 status = '사용가능'

-- 기장
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 기장 충전소' limit 1), '기장 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 기장 충전소' limit 1), '기장 급속 2번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 기장 충전소' limit 1), '기장 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 해운대
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 해운대 충전소' limit 1), '해운대 초급속 1번', '초급속', 'DC콤보', 200.00, 390.00, '사용가능'),
((select station_id from charging_station where station_name = '테스트 해운대 충전소' limit 1), '해운대 급속 2번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 해운대 충전소' limit 1), '해운대 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 광안리
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 광안리 충전소' limit 1), '광안리 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 광안리 충전소' limit 1), '광안리 급속 2번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 광안리 충전소' limit 1), '광안리 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 남포
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 남포 충전소' limit 1), '남포 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 남포 충전소' limit 1), '남포 완속 2번', '완속', 'AC완속', 7.00, 280.00, '사용가능'),
((select station_id from charging_station where station_name = '테스트 남포 충전소' limit 1), '남포 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 부산역
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 부산역 충전소' limit 1), '부산역 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 부산역 충전소' limit 1), '부산역 완속 2번', '완속', 'AC완속', 7.00, 280.00, '사용가능'),
((select station_id from charging_station where station_name = '테스트 부산역 충전소' limit 1), '부산역 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 서면
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 서면 충전소' limit 1), '서면 초급속 1번', '초급속', 'DC콤보', 200.00, 390.00, '사용가능'),
((select station_id from charging_station where station_name = '테스트 서면 충전소' limit 1), '서면 급속 2번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 서면 충전소' limit 1), '서면 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 사상
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 사상 충전소' limit 1), '사상 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 사상 충전소' limit 1), '사상 완속 2번', '완속', 'AC완속', 7.00, 280.00, '사용가능'),
((select station_id from charging_station where station_name = '테스트 사상 충전소' limit 1), '사상 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 덕천
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 덕천 충전소' limit 1), '덕천 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 덕천 충전소' limit 1), '덕천 급속 2번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 덕천 충전소' limit 1), '덕천 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 명지
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 명지 충전소' limit 1), '명지 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 명지 충전소' limit 1), '명지 완속 2번', '완속', 'AC완속', 7.00, 280.00, '사용가능'),
((select station_id from charging_station where station_name = '테스트 명지 충전소' limit 1), '명지 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 다대포
insert into charger (station_id, charger_name, charger_type, connector_type, charging_speed_kw, price_per_kwh, status)
values
((select station_id from charging_station where station_name = '테스트 다대포 충전소' limit 1), '다대포 급속 1번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 다대포 충전소' limit 1), '다대포 급속 2번', '급속', 'DC콤보', 100.00, 347.20, '사용가능'),
((select station_id from charging_station where station_name = '테스트 다대포 충전소' limit 1), '다대포 완속 3번', '완속', 'AC완속', 7.00, 280.00, '사용가능');

-- 5. 결과 확인
select
      cs.station_id
    , cs.station_name
    , cs.address
    , cs.image_url
    , count(c.charger_id) as charger_count
    , sum(case when c.status = '사용가능' then 1 else 0 end) as available_charger_count
from charging_station cs
left join charger c
  on cs.station_id = c.station_id
group by
      cs.station_id
    , cs.station_name
    , cs.address
    , cs.image_url
order by cs.station_id;
