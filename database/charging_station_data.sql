-- =========================================================
-- 부산 테스트용 충전소 기존 데이터 제거
-- charging_station 삭제 시 charger는 on delete cascade로 같이 삭제됨
-- =========================================================
delete from charging_station
where station_name in (
      '테스트 부산역 충전소'
    , '테스트 서면 충전소'
    , '테스트 해운대 충전소'
    , '테스트 광안리 충전소'
    , '테스트 사상 충전소'
);

-- =========================================================
-- 부산 테스트용 충전소 5개 추가
-- ST_MakePoint(경도, 위도) 순서 주의
-- =========================================================
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
)
values
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
);

-- =========================================================
-- 각 충전소별 충전기 추가
-- =========================================================

-- 부산역 충전소
insert into charger (
      station_id
    , charger_name
    , charger_type
    , connector_type
    , charging_speed_kw
    , price_per_kwh
    , status
)
values
(
      (select station_id from charging_station where station_name = '테스트 부산역 충전소' limit 1)
    , '부산역 급속 1번'
    , '급속'
    , 'DC콤보'
    , 100.00
    , 347.20
    , '사용가능'
),
(
      (select station_id from charging_station where station_name = '테스트 부산역 충전소' limit 1)
    , '부산역 완속 2번'
    , '완속'
    , 'AC완속'
    , 7.00
    , 280.00
    , '사용가능'
);

-- 서면 충전소
insert into charger (
      station_id
    , charger_name
    , charger_type
    , connector_type
    , charging_speed_kw
    , price_per_kwh
    , status
)
values
(
      (select station_id from charging_station where station_name = '테스트 서면 충전소' limit 1)
    , '서면 급속 1번'
    , '급속'
    , 'DC콤보'
    , 100.00
    , 347.20
    , '사용가능'
),
(
      (select station_id from charging_station where station_name = '테스트 서면 충전소' limit 1)
    , '서면 초급속 2번'
    , '초급속'
    , 'DC콤보'
    , 200.00
    , 390.00
    , '예약중'
);

-- 해운대 충전소
insert into charger (
      station_id
    , charger_name
    , charger_type
    , connector_type
    , charging_speed_kw
    , price_per_kwh
    , status
)
values
(
      (select station_id from charging_station where station_name = '테스트 해운대 충전소' limit 1)
    , '해운대 초급속 1번'
    , '초급속'
    , 'DC콤보'
    , 200.00
    , 390.00
    , '사용가능'
),
(
      (select station_id from charging_station where station_name = '테스트 해운대 충전소' limit 1)
    , '해운대 급속 2번'
    , '급속'
    , 'DC콤보'
    , 100.00
    , 347.20
    , '사용가능'
);

-- 광안리 충전소
insert into charger (
      station_id
    , charger_name
    , charger_type
    , connector_type
    , charging_speed_kw
    , price_per_kwh
    , status
)
values
(
      (select station_id from charging_station where station_name = '테스트 광안리 충전소' limit 1)
    , '광안리 급속 1번'
    , '급속'
    , 'DC콤보'
    , 100.00
    , 347.20
    , '사용가능'
),
(
      (select station_id from charging_station where station_name = '테스트 광안리 충전소' limit 1)
    , '광안리 완속 2번'
    , '완속'
    , 'AC완속'
    , 7.00
    , 280.00
    , '점검중'
);

-- 사상 충전소
insert into charger (
      station_id
    , charger_name
    , charger_type
    , connector_type
    , charging_speed_kw
    , price_per_kwh
    , status
)
values
(
      (select station_id from charging_station where station_name = '테스트 사상 충전소' limit 1)
    , '사상 급속 1번'
    , '급속'
    , 'DC콤보'
    , 100.00
    , 347.20
    , '사용가능'
),
(
      (select station_id from charging_station where station_name = '테스트 사상 충전소' limit 1)
    , '사상 완속 2번'
    , '완속'
    , 'AC완속'
    , 7.00
    , 280.00
    , '고장'
);