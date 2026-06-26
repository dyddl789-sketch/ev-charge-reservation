-- 공공 전기차 충전 인프라 운영 MIS 조회 성능 개선용 인덱스
-- PostgreSQL / PostGIS 기준. 운영 DB에는 DBeaver 또는 배포 스크립트로 1회 적용한다.

create extension if not exists pg_trgm;

-- 회원관리 검색/필터
create index if not exists idx_member_status_created on app_member(status, created_at desc);
create index if not exists idx_member_user_type_created on app_member(user_type, created_at desc);
create index if not exists idx_member_created_at on app_member(created_at desc);
create index if not exists idx_member_user_id_trgm on app_member using gin (user_id gin_trgm_ops);
create index if not exists idx_member_name_trgm on app_member using gin (member_name gin_trgm_ops);
create index if not exists idx_member_nickname_trgm on app_member using gin (nickname gin_trgm_ops);
create index if not exists idx_member_email_trgm on app_member using gin (email gin_trgm_ops);
create index if not exists idx_member_phone_trgm on app_member using gin (phone gin_trgm_ops);

-- 예약관리 검색/필터
create index if not exists idx_reservation_member_created on reservation(member_id, created_at desc);
create index if not exists idx_reservation_status_start_time on reservation(status, start_time desc);
create index if not exists idx_reservation_date_status on reservation(reservation_date, status);
create index if not exists idx_reservation_start_time on reservation(start_time desc);

-- 통계분석 완료 충전 세션 부분 인덱스
create index if not exists idx_session_completed_start_time on charging_session(actual_start_time desc) where status = '완료';
create index if not exists idx_session_completed_charger_time on charging_session(charger_id, actual_start_time desc) where status = '완료';
create index if not exists idx_session_completed_member_time on charging_session(member_id, actual_start_time desc) where status = '완료';

-- 충전소관리 문자열 검색/필터
create index if not exists idx_station_name_trgm on charging_station using gin (station_name gin_trgm_ops);
create index if not exists idx_station_address_trgm on charging_station using gin (address gin_trgm_ops);
create index if not exists idx_station_operator_trgm on charging_station using gin (operator_name gin_trgm_ops);
create index if not exists idx_station_status_source on charging_station(station_status, source_type, is_deleted);
create index if not exists idx_charger_station_type_status on charger(station_id, charger_type, status);

-- 위치 기반 조회
create index if not exists idx_station_location_gist on charging_station using gist(location);
create index if not exists idx_saved_location_gist on saved_location using gist(location);

-- 민원/장애 조회 보강
create index if not exists idx_complaint_status_type_created on complaint(status, complaint_type, created_at desc);
create index if not exists idx_complaint_member_created on complaint(member_id, created_at desc);
create index if not exists idx_fault_charger_status on fault_report(charger_id, status);
