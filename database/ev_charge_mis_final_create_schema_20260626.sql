/* =========================================================
   EV Charge Reservation + 공공 전기차 충전 인프라 운영 MIS
   최종 통합 DB 생성 SQL

   생성일: 2026-06-26

   실행 환경
   - PostgreSQL 16+
   - PostGIS 확장 필요
   - DBeaver에서 ev_charge_mis DB 선택 후 전체 실행

   포함 내용
   1. 기본 EV Charge / MIS 전체 스키마
   2. 공지사항 게시판 및 기본 공지 데이터
   3. AI 채팅 메시지 카드 데이터 유지용 컬럼
   4. 전자결재 상신/승인/서명 흐름 보강
   5. 직원 계정/내 정보 관리 보강

   주의
   - 기본적으로 create table if not exists / add column if not exists를 사용합니다.
   - 이미 존재하는 데이터는 최대한 유지합니다.
   - 완전 초기화가 필요한 경우 DB 백업 후 별도 DROP 스크립트를 먼저 실행하세요.
========================================================= */



/* =========================================================
   SECTION 1. 기본 전체 스키마
   원본 파일: ev_charge_mis_full_schema.sql
========================================================= */

/* =========================================================
   EV Charge Reservation v2.0 + 공공 전기차 충전 인프라 운영 MIS
   전체 테이블 생성 SQL

   목적
   - 기존 EV Charge Reservation 기본 테이블 생성
   - MIS 확장 테이블 생성
   - PK / FK / CHECK / UNIQUE / INDEX 관계 포함

   사용 위치
   - PostgreSQL 16 + PostGIS 환경
   - DBeaver에서 대상 DB 선택 후 전체 실행

   주의
   - GitHub 저장용 전체 스키마 파일입니다.
   - 기존 데이터가 있는 DB에서 실행해도 최대한 안전하도록 IF NOT EXISTS를 사용했습니다.
   - 완전 초기화가 필요한 경우에는 별도 DROP TABLE 스크립트를 먼저 실행하세요.
========================================================= */


/* =========================================================
   PART 1. EV Charge 기본 서비스 테이블
========================================================= */

-- PostGIS 확장 기능 활성화
-- geography(Point, 4326) 타입을 사용하기 위해 필요함
-- 충전소와 사용자 위치의 거리 계산, 주변 충전소 검색 등에 사용
create extension if not exists postgis;

------------------------------------------------------------------------------------------------------------------------
-- 회원 정보
create table if not exists app_member (
    member_id bigint generated always as identity primary key, -- 회원 고유 번호

    user_id varchar(50) not null unique,                       -- 로그인 아이디
    password varchar(255),                                     -- 암호화 비밀번호 (소셜 로그인 시 null 가능)

    member_name varchar(50) not null,                          -- 회원 이름
    nickname varchar(50),                                      -- 닉네임

    phone varchar(20) unique,                                  -- 전화번호
    email varchar(100) not null unique,                        -- 이메일

    profile_image_url varchar(500),                            -- 프로필 이미지 URL

    user_type varchar(20) not null default 'USER',             -- USER / ADMIN
    login_type varchar(20) not null default 'LOCAL',           -- LOCAL / GOOGLE / KAKAO / NAVER

    status varchar(20) not null default 'ACTIVE',              -- ACTIVE / INACTIVE / BLOCKED

    last_login_at timestamp,                                   -- 마지막 로그인 시간

    created_at timestamp not null default current_timestamp,   -- 가입일
    updated_at timestamp,                                      -- 수정일

    constraint chk_member_user_type
        check (user_type in ('USER', 'ADMIN')),

    constraint chk_member_login_type
        check (login_type in ('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER')),

    constraint chk_member_status
        check (status in ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);

comment on table app_member is '회원 정보를 저장하는 테이블';

------------------------------------------------------------------------------------------------------------------------
-- 출발 기준 위치 저장
create table if not exists saved_location (
    location_id bigint generated always as identity primary key, -- 위치 고유 번호

    member_id bigint not null,                                  -- 회원 번호

    location_name varchar(50) not null,                         -- 위치 이름: 집, 회사, 강남역 등

    location_type varchar(20) not null default 'CUSTOM',        -- 위치 유형: HOME, WORK, SCHOOL, FAVORITE, CUSTOM

    address varchar(255) not null,                              -- 주소

    latitude double precision not null,                         -- 위도
    longitude double precision not null,                        -- 경도

    location geography(Point, 4326) not null,                   -- PostGIS 위치 정보

    is_default boolean not null default false,                  -- 기본 위치 여부

    created_at timestamp not null default current_timestamp,    -- 등록일
    updated_at timestamp,                                       -- 수정일

    constraint fk_saved_location_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint chk_saved_location_type
        check (location_type in ('HOME', 'WORK', 'SCHOOL', 'FAVORITE', 'CUSTOM')),

    constraint chk_saved_location_latitude
        check (latitude between -90 and 90),

    constraint chk_saved_location_longitude
        check (longitude between -180 and 180)
);

comment on table saved_location is '회원이 저장한 출발 위치 정보를 저장하는 테이블';

------------------------------------------------------------------------------------------------------------------------
-- 차량 모델 마스터
create table if not exists vehicle_model (
    model_id bigint generated always as identity primary key, -- 차량 모델 고유 번호

    manufacturer varchar(50) not null,                       -- 제조사: 현대, 기아, 테슬라 등
    model_name varchar(100) not null,                        -- 차량 모델명: 아이오닉5, EV6 등

    battery_capacity_kwh numeric(6,2) not null,              -- 배터리 총 용량 kWh
    connector_type varchar(50) not null,                     -- 커넥터 타입: DC콤보, NACS 등
    max_charging_speed_kw numeric(6,2),                      -- 최대 충전 속도 kW

    created_at timestamp not null default current_timestamp, -- 등록일
    updated_at timestamp,                                    -- 수정일

    constraint uq_vehicle_model
        unique (manufacturer, model_name),

    constraint chk_vehicle_model_battery_capacity
        check (battery_capacity_kwh > 0),

    constraint chk_vehicle_model_max_speed
        check (max_charging_speed_kw is null or max_charging_speed_kw > 0)
);

comment on table vehicle_model is '전기차 모델 기본 정보를 저장하는 마스터 테이블';

alter table vehicle_model
add column if not exists image_url varchar(500);

------------------------------------------------------------------------------------------------------------------------
-- 사용자 등록 차량
create table if not exists vehicle (
    vehicle_id bigint generated always as identity primary key, -- 차량 고유 번호

    member_id bigint not null,                                 -- 차량을 등록한 회원 번호
    model_id bigint not null,                                  -- 차량 모델 번호

    vehicle_nickname varchar(100),                             -- 차량 별칭: 내차, 회사차 등
    plate_number varchar(20),                                  -- 차량 번호

    is_default boolean not null default false,                 -- 기본 차량 여부

    created_at timestamp not null default current_timestamp,   -- 차량 등록일
    updated_at timestamp,                                      -- 차량 정보 수정일

    constraint fk_vehicle_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_vehicle_model
        foreign key (model_id)
        references vehicle_model(model_id),

    constraint uq_vehicle_plate_number
        unique (member_id, plate_number)
);

comment on table vehicle is '회원이 등록한 전기차 정보를 저장하는 테이블';

--논리삭제
alter table vehicle
add column if not exists is_deleted boolean not null default false;
--논리삭제시간
alter table vehicle
add column if not exists deleted_at timestamp;

------------------------------------------------------------------------------------------------------------------------
-- 충전소
create table if not exists charging_station (
    station_id bigint generated always as identity primary key, -- 충전소 고유 번호

    station_name varchar(100) not null,                        -- 충전소 이름
    address varchar(255) not null,                             -- 충전소 주소

    latitude double precision not null,                        -- 충전소 위도
    longitude double precision not null,                       -- 충전소 경도
    location geography(Point, 4326) not null,                  -- PostGIS 거리 계산용 위치 정보

    operator_name varchar(100),                                -- 운영기관 또는 사업자명

    open_time time,                                            -- 운영 시작 시간
    close_time time,                                           -- 운영 종료 시간

    station_status varchar(20) not null default '운영중',       -- 충전소 상태: 운영중, 점검중, 운영중지

    created_at timestamp not null default current_timestamp,   -- 충전소 등록일
    updated_at timestamp,                                      -- 충전소 정보 수정일

    constraint chk_station_status
        check (station_status in ('운영중', '점검중', '운영중지')),

    constraint chk_station_latitude
        check (latitude between -90 and 90),

    constraint chk_station_longitude
        check (longitude between -180 and 180)
);

comment on table charging_station is '전기차 충전소 정보를 저장하는 테이블';

------------------------------------------------------------------------------------------------------------------------
-- 충전기
create table if not exists charger (
    charger_id bigint generated always as identity primary key, -- 충전기 고유 번호

    station_id bigint not null,                                -- 충전기가 소속된 충전소 번호

    charger_name varchar(100) not null,                        -- 충전기 이름 또는 번호
    charger_type varchar(20) not null,                         -- 충전기 유형: 완속, 급속, 초급속
    connector_type varchar(50) not null,                       -- 충전기 커넥터 타입
    charging_speed_kw numeric(6,2) not null,                   -- 충전기 출력 kW
    price_per_kwh numeric(8,2) not null,                       -- kWh당 충전 요금

    status varchar(20) not null default '사용가능',             -- 상태: 사용가능, 예약중, 사용중, 점검중, 고장

    created_at timestamp not null default current_timestamp,   -- 충전기 등록일
    updated_at timestamp,                                      -- 충전기 정보 수정일

    constraint fk_charger_station
        foreign key (station_id)
        references charging_station(station_id)
        on delete cascade,

    constraint chk_charger_type
        check (charger_type in ('완속', '급속', '초급속')),

    constraint chk_charger_speed
        check (charging_speed_kw > 0),

    constraint chk_charger_price
        check (price_per_kwh >= 0),

    constraint chk_charger_status
        check (status in ('사용가능', '예약중', '사용중', '점검중', '고장'))
);

comment on table charger is '충전소에 설치된 충전기 정보를 저장하는 테이블';

------------------------------------------------------------------------------------------------------------------------
-- 예약
create table if not exists reservation (
    reservation_id bigint generated always as identity primary key, -- 예약 고유 번호

    member_id bigint not null,                                     -- 예약한 회원 번호
    vehicle_id bigint not null,                                    -- 예약에 사용한 차량 번호
    charger_id bigint not null,                                    -- 예약한 충전기 번호

    reservation_date date not null,                                -- 예약 날짜
    start_time timestamp not null,                                 -- 예약 시작 일시
    end_time timestamp not null,                                   -- 예약 종료 일시

    current_soc int not null,                                      -- 현재 배터리 잔량 %
    target_soc int not null,                                       -- 목표 배터리 잔량 %
    required_kwh numeric(8,2) not null,                            -- 필요한 예상 충전량 kWh
    estimated_minutes int not null,                                -- 예상 충전 시간 분
    estimated_cost numeric(10,2) not null,                         -- 예상 충전 비용

    status varchar(20) not null default '예약완료',                 -- 예약 상태: 예약완료, 인증완료, 충전중, 완료, 취소, 노쇼

    auth_code varchar(20) unique,                                  -- 예약 인증 코드
    verified_at timestamp,                                         -- 예약 인증 완료 시간
    no_show_at timestamp,                                          -- 노쇼 처리 시간
    canceled_at timestamp,                                         -- 예약 취소 시간

    created_at timestamp not null default current_timestamp,       -- 예약 등록일
    updated_at timestamp,                                          -- 예약 수정일

    constraint fk_reservation_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_reservation_vehicle
        foreign key (vehicle_id)
        references vehicle(vehicle_id)
        on delete cascade,

    constraint fk_reservation_charger
        foreign key (charger_id)
        references charger(charger_id)
        on delete cascade,

    constraint chk_reservation_soc
        check (
            current_soc >= 0
            and current_soc <= 100
            and target_soc >= 0
            and target_soc <= 100
            and target_soc > current_soc
        ),

    constraint chk_reservation_time
        check (end_time > start_time),

    constraint chk_reservation_required_kwh
        check (required_kwh > 0),

    constraint chk_reservation_minutes
        check (estimated_minutes > 0),

    constraint chk_reservation_cost
        check (estimated_cost >= 0),

    constraint chk_reservation_status
        check (status in ('예약완료', '인증완료', '충전중', '완료', '취소', '노쇼'))
);

comment on table reservation is '전기차 충전 예약 정보를 저장하는 테이블';

-- 예약 시간 검색 속도 향상용 인덱스
create index if not exists idx_reservation_charger_time
on reservation(charger_id, start_time, end_time);

------------------------------------------------------------------------------------------------------------------------
-- 충전 세션
create table if not exists charging_session (
    session_id bigint generated always as identity primary key,     -- 충전 세션 고유 번호

    reservation_id bigint not null unique,                         -- 연결된 예약 번호
    member_id bigint not null,                                     -- 충전한 회원 번호
    vehicle_id bigint not null,                                    -- 충전에 사용한 차량 번호
    charger_id bigint not null,                                    -- 실제 사용한 충전기 번호

    actual_start_time timestamp not null,                          -- 실제 충전 시작 일시
    actual_end_time timestamp,                                     -- 실제 충전 종료 일시

    start_soc int not null,                                        -- 실제 충전 시작 시 배터리 잔량 %
    end_soc int,                                                   -- 실제 충전 종료 시 배터리 잔량 %

    actual_kwh numeric(8,2),                                       -- 실제 충전량 kWh
    actual_minutes int,                                            -- 실제 충전 시간 분
    actual_cost numeric(10,2),                                     -- 실제 충전 비용

    status varchar(20) not null default '충전중',                   -- 충전 상태: 충전중, 완료, 중단

    created_at timestamp not null default current_timestamp,       -- 세션 생성일
    updated_at timestamp,                                          -- 세션 수정일

    constraint fk_session_reservation
        foreign key (reservation_id)
        references reservation(reservation_id)
        on delete cascade,

    constraint fk_session_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_session_vehicle
        foreign key (vehicle_id)
        references vehicle(vehicle_id)
        on delete cascade,

    constraint fk_session_charger
        foreign key (charger_id)
        references charger(charger_id)
        on delete cascade,

    constraint chk_session_soc
        check (
            start_soc >= 0
            and start_soc <= 100
            and (end_soc is null or (end_soc >= 0 and end_soc <= 100))
            and (end_soc is null or end_soc >= start_soc)
        ),

    constraint chk_session_time
        check (
            actual_end_time is null
            or actual_end_time > actual_start_time
        ),

    constraint chk_session_kwh
        check (actual_kwh is null or actual_kwh >= 0),

    constraint chk_session_minutes
        check (actual_minutes is null or actual_minutes >= 0),

    constraint chk_session_cost
        check (actual_cost is null or actual_cost >= 0),

    constraint chk_session_status
        check (status in ('충전중', '완료', '중단'))
);

comment on table charging_session is '전기차 실제 충전 사용 기록을 저장하는 테이블';

-- 사용자 충전 이력 조회 속도를 높이기 위한 인덱스
create index if not exists idx_charging_session_member
on charging_session(member_id);

create index if not exists idx_charging_session_charger
on charging_session(charger_id);

------------------------------------------------------------------------------------------------------------------------
-- 즐겨찾기
create table if not exists favorite_station (
    favorite_id bigint generated always as identity primary key, -- 즐겨찾기 고유 번호

    member_id bigint not null,                                  -- 회원 번호
    station_id bigint not null,                                 -- 즐겨찾기한 충전소 번호

    created_at timestamp not null default current_timestamp,    -- 즐겨찾기 등록일

    constraint fk_favorite_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_favorite_station
        foreign key (station_id)
        references charging_station(station_id)
        on delete cascade,

    constraint uq_favorite_member_station
        unique (member_id, station_id)
);

comment on table favorite_station is '회원이 즐겨찾기한 충전소 정보를 저장하는 테이블';

--AI 채팅방 (사용자별 AI 대화방 관리)
create table if not exists ai_chat_room (
    room_id bigint generated always as identity primary key,

    member_id bigint not null,

    title varchar(100),

    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_ai_chat_room_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade
);
--AI 채팅 메시지(AI 대화 기록 저장)
create table if not exists ai_chat_message (
    message_id bigint generated always as identity primary key,

    room_id bigint not null,

    sender_type varchar(20) not null, -- USER / AI

    message text not null,

    created_at timestamp not null default current_timestamp,

    constraint fk_ai_chat_message_room
        foreign key (room_id)
        references ai_chat_room(room_id)
        on delete cascade,

    constraint chk_ai_chat_sender
        check (sender_type in ('USER', 'AI'))
);

alter table charging_station
add column if not exists image_url varchar(255)
default '/images/station/station-default.jpg';


/* =========================================================
   PART 2. 공공기관 MIS 확장 테이블
========================================================= */

/* =========================================================
   EV Charge MIS 확장 SQL - 최종 적용본
   적용 대상 DB: ev_charge_mis

   사용 방법
   1) DBeaver에서 ev_charge_mis 연결 선택
   2) SQL Editor 열기
   3) 이 파일 전체 실행

   특징
   - 기존 ev_charge_mis에 복제된 기본 테이블은 삭제/재생성하지 않음
   - MIS 기능에 필요한 테이블만 추가
   - 프론트 화면 기준: 관리자 대시보드, 회원관리, 민원관리, 전자결재, 공지사항, 충전소/충전기 관리 확장
========================================================= */

------------------------------------------------------------------------------------------------------------------------
-- 0. 기존 회원 권한 확장
-- USER: 일반 사용자
-- ADMIN: 기관장/최고관리자
-- MANAGER: 운영관리자
-- OPERATOR: 운영담당자
-- ENGINEER: 시설관리담당자
------------------------------------------------------------------------------------------------------------------------
alter table app_member
    drop constraint if exists chk_member_user_type;

alter table app_member
    add constraint chk_member_user_type
    check (user_type in ('USER', 'ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'));

comment on column app_member.user_type is 'USER, ADMIN, MANAGER, OPERATOR, ENGINEER';

------------------------------------------------------------------------------------------------------------------------
-- 1. 기존 충전소 / 충전기 테이블 확장
-- 환경부 Open API 연동, 관리자 충전 인프라 관리, 지도 화면 표시를 위한 보조 컬럼
------------------------------------------------------------------------------------------------------------------------
alter table charging_station
    add column if not exists external_station_id varchar(100),
    add column if not exists operator_code varchar(50),
    add column if not exists source_type varchar(30) not null default 'LOCAL',
    add column if not exists use_time varchar(100),
    add column if not exists parking_free boolean,
    add column if not exists detail_location varchar(255),
    add column if not exists contact_phone varchar(30),
    add column if not exists is_deleted boolean not null default false,
    add column if not exists deleted_at timestamp;

comment on column charging_station.external_station_id is '외부 API 충전소 ID';
comment on column charging_station.source_type is 'LOCAL / PUBLIC_API';
comment on column charging_station.use_time is '운영 시간 원문';
comment on column charging_station.parking_free is '주차 무료 여부';
comment on column charging_station.detail_location is '상세 위치 설명';
comment on column charging_station.contact_phone is '충전소 연락처';

alter table charger
    add column if not exists external_charger_id varchar(100),
    add column if not exists charger_code varchar(50),
    add column if not exists last_status_checked_at timestamp,
    add column if not exists is_deleted boolean not null default false,
    add column if not exists deleted_at timestamp;

comment on column charger.external_charger_id is '외부 API 충전기 ID';
comment on column charger.charger_code is '충전기 관리 코드';
comment on column charger.last_status_checked_at is '충전기 상태 마지막 확인 시간';

create index if not exists idx_station_external_id on charging_station(external_station_id);
create index if not exists idx_station_source_type on charging_station(source_type);
create index if not exists idx_station_deleted on charging_station(is_deleted);
create index if not exists idx_charger_external_id on charger(external_charger_id);
create index if not exists idx_charger_status on charger(status);
create index if not exists idx_charger_deleted on charger(is_deleted);

------------------------------------------------------------------------------------------------------------------------
-- 2. 부서 정보
------------------------------------------------------------------------------------------------------------------------
create table if not exists department (
    department_id bigint generated always as identity primary key,
    department_name varchar(100) not null,
    department_code varchar(50) not null unique,
    description varchar(500),
    is_active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp
);

comment on table department is '운영기관 부서 정보를 저장하는 테이블';
comment on column department.department_name is '부서명: 운영팀, 시설관리팀 등';
comment on column department.department_code is '부서 코드';

------------------------------------------------------------------------------------------------------------------------
-- 3. 직원 정보
-- app_member와 연결해서 관리자/운영자 계정을 직원으로 관리
------------------------------------------------------------------------------------------------------------------------
create table if not exists employee (
    employee_id bigint generated always as identity primary key,
    member_id bigint not null unique,
    department_id bigint not null,
    employee_no varchar(50) not null unique,
    position_name varchar(50) not null,
    duty_name varchar(100),
    status varchar(20) not null default 'ACTIVE',
    hired_at date,
    retired_at date,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_employee_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_employee_department
        foreign key (department_id)
        references department(department_id),

    constraint chk_employee_status
        check (status in ('ACTIVE', 'INACTIVE', 'RETIRED'))
);

comment on table employee is '운영기관 직원 정보를 저장하는 테이블';
comment on column employee.member_id is 'app_member와 연결되는 회원 번호';
comment on column employee.duty_name is '담당 업무명';

create index if not exists idx_employee_department on employee(department_id);
create index if not exists idx_employee_status on employee(status);

------------------------------------------------------------------------------------------------------------------------
-- 4. 공지사항
-- 프론트 NoticeSection / NoticePage / NoticeDetailPage 기준
------------------------------------------------------------------------------------------------------------------------
create table if not exists notice (
    notice_id bigint generated always as identity primary key,
    writer_id bigint,
    title varchar(200) not null,
    content text not null,
    category varchar(30) not null default '공지',
    status varchar(20) not null default '게시',
    is_pinned boolean not null default false,
    is_public boolean not null default true,
    view_count int not null default 0,
    start_at timestamp,
    end_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_notice_writer
        foreign key (writer_id)
        references employee(employee_id),

    constraint chk_notice_status
        check (status in ('임시저장', '게시', '숨김', '삭제'))
);

comment on table notice is '공지사항 정보를 저장하는 테이블';
comment on column notice.category is '공지 카테고리: 공지, 점검, 안내 등';

create index if not exists idx_notice_status on notice(status);
create index if not exists idx_notice_pinned on notice(is_pinned);
create index if not exists idx_notice_created_at on notice(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 5. FAQ
-- 고객센터 FAQ 화면을 DB 기반으로 전환할 때 사용
------------------------------------------------------------------------------------------------------------------------
create table if not exists faq (
    faq_id bigint generated always as identity primary key,
    category varchar(50) not null default '이용안내',
    question varchar(300) not null,
    answer text not null,
    display_order int not null default 0,
    is_active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp
);

comment on table faq is '고객센터 자주 묻는 질문 정보를 저장하는 테이블';

create index if not exists idx_faq_active_order on faq(is_active, display_order);

------------------------------------------------------------------------------------------------------------------------
-- 6. 민원 정보
-- 프론트 ComplaintPage 입력값: complaintType, stationName, chargerName, title, content, priority, notifyEmail/Sms/Site 반영
------------------------------------------------------------------------------------------------------------------------
create table if not exists complaint (
    complaint_id bigint generated always as identity primary key,
    member_id bigint not null,

    title varchar(200) not null,
    content text not null,

    complaint_type varchar(50) not null,
    priority varchar(20) not null default 'NORMAL',
    status varchar(30) not null default '접수',

    station_id bigint,
    charger_id bigint,
    station_name varchar(100),
    charger_name varchar(100),

    notify_email boolean not null default false,
    notify_sms boolean not null default false,
    notify_site boolean not null default true,

    assigned_department_id bigint,
    assigned_employee_id bigint,

    ai_summary text,
    admin_memo text,

    created_at timestamp not null default current_timestamp,
    updated_at timestamp,
    closed_at timestamp,

    constraint fk_complaint_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_complaint_station
        foreign key (station_id)
        references charging_station(station_id),

    constraint fk_complaint_charger
        foreign key (charger_id)
        references charger(charger_id),

    constraint fk_complaint_department
        foreign key (assigned_department_id)
        references department(department_id),

    constraint fk_complaint_employee
        foreign key (assigned_employee_id)
        references employee(employee_id),

    constraint chk_complaint_priority
        check (priority in ('LOW', 'NORMAL', 'HIGH', 'URGENT')),

    constraint chk_complaint_status
        check (status in ('접수', '확인중', '배정', '처리중', '완료', '반려', '취소'))
);

comment on table complaint is '사용자 민원 정보를 저장하는 테이블';
comment on column complaint.station_name is '사용자가 직접 입력한 충전소명 또는 화면에서 선택한 충전소명';
comment on column complaint.charger_name is '사용자가 직접 입력한 충전기명 또는 화면에서 선택한 충전기명';
comment on column complaint.ai_summary is 'AI 민원 요약 내용';

create index if not exists idx_complaint_member on complaint(member_id);
create index if not exists idx_complaint_status on complaint(status);
create index if not exists idx_complaint_type on complaint(complaint_type);
create index if not exists idx_complaint_priority on complaint(priority);
create index if not exists idx_complaint_assigned_employee on complaint(assigned_employee_id);
create index if not exists idx_complaint_created_at on complaint(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 7. 민원 처리 이력
------------------------------------------------------------------------------------------------------------------------
create table if not exists complaint_history (
    history_id bigint generated always as identity primary key,
    complaint_id bigint not null,
    employee_id bigint,
    before_status varchar(30),
    after_status varchar(30),
    action_type varchar(50) not null,
    memo text,
    created_at timestamp not null default current_timestamp,

    constraint fk_complaint_history_complaint
        foreign key (complaint_id)
        references complaint(complaint_id)
        on delete cascade,

    constraint fk_complaint_history_employee
        foreign key (employee_id)
        references employee(employee_id)
);

comment on table complaint_history is '민원 처리 이력을 저장하는 테이블';

create index if not exists idx_complaint_history_complaint on complaint_history(complaint_id);
create index if not exists idx_complaint_history_created_at on complaint_history(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 8. AI 민원 분류 결과
------------------------------------------------------------------------------------------------------------------------
create table if not exists ai_classification (
    classification_id bigint generated always as identity primary key,
    complaint_id bigint not null,
    predicted_type varchar(50) not null,
    predicted_department varchar(100),
    predicted_priority varchar(20),
    confidence numeric(5,2),
    summary text,
    raw_response text,
    created_at timestamp not null default current_timestamp,

    constraint fk_ai_classification_complaint
        foreign key (complaint_id)
        references complaint(complaint_id)
        on delete cascade
);

comment on table ai_classification is 'AI 민원 유형 분류 결과를 저장하는 테이블';

create index if not exists idx_ai_classification_complaint on ai_classification(complaint_id);

------------------------------------------------------------------------------------------------------------------------
-- 9. 장애 정보
-- 민원에서 시설장애로 판단되면 장애관리로 연결
------------------------------------------------------------------------------------------------------------------------
create table if not exists fault_report (
    fault_id bigint generated always as identity primary key,
    complaint_id bigint,
    station_id bigint not null,
    charger_id bigint not null,

    fault_type varchar(50) not null,
    title varchar(200) not null,
    description text,
    severity varchar(20) not null default 'NORMAL',
    status varchar(30) not null default '접수',
    source_type varchar(30) not null default '민원',

    assigned_employee_id bigint,

    reported_at timestamp not null default current_timestamp,
    resolved_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_fault_complaint
        foreign key (complaint_id)
        references complaint(complaint_id),

    constraint fk_fault_station
        foreign key (station_id)
        references charging_station(station_id),

    constraint fk_fault_charger
        foreign key (charger_id)
        references charger(charger_id),

    constraint fk_fault_employee
        foreign key (assigned_employee_id)
        references employee(employee_id),

    constraint chk_fault_severity
        check (severity in ('LOW', 'NORMAL', 'HIGH', 'CRITICAL')),

    constraint chk_fault_status
        check (status in ('접수', '점검중', '조치중', '결재대기', '완료', '취소')),

    constraint chk_fault_source_type
        check (source_type in ('민원', '수동등록', '시스템감지'))
);

comment on table fault_report is '충전기 장애 정보를 저장하는 테이블';

create index if not exists idx_fault_complaint on fault_report(complaint_id);
create index if not exists idx_fault_station on fault_report(station_id);
create index if not exists idx_fault_charger on fault_report(charger_id);
create index if not exists idx_fault_status on fault_report(status);
create index if not exists idx_fault_assigned_employee on fault_report(assigned_employee_id);
create index if not exists idx_fault_reported_at on fault_report(reported_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 10. 장애 처리 이력
------------------------------------------------------------------------------------------------------------------------
create table if not exists fault_history (
    history_id bigint generated always as identity primary key,
    fault_id bigint not null,
    employee_id bigint,
    before_status varchar(30),
    after_status varchar(30),
    action_type varchar(50) not null,
    memo text,
    created_at timestamp not null default current_timestamp,

    constraint fk_fault_history_fault
        foreign key (fault_id)
        references fault_report(fault_id)
        on delete cascade,

    constraint fk_fault_history_employee
        foreign key (employee_id)
        references employee(employee_id)
);

comment on table fault_history is '장애 처리 이력을 저장하는 테이블';

create index if not exists idx_fault_history_fault on fault_history(fault_id);
create index if not exists idx_fault_history_created_at on fault_history(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 11. 점검 정보
------------------------------------------------------------------------------------------------------------------------
create table if not exists inspection (
    inspection_id bigint generated always as identity primary key,
    fault_id bigint,
    charger_id bigint not null,
    inspector_id bigint not null,

    inspection_type varchar(30) not null default '장애점검',
    inspection_result varchar(30) not null default '진행중',
    description text,
    action_required boolean not null default false,

    scheduled_at timestamp,
    inspection_date timestamp not null default current_timestamp,
    completed_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_inspection_fault
        foreign key (fault_id)
        references fault_report(fault_id),

    constraint fk_inspection_charger
        foreign key (charger_id)
        references charger(charger_id),

    constraint fk_inspection_employee
        foreign key (inspector_id)
        references employee(employee_id),

    constraint chk_inspection_type
        check (inspection_type in ('정기점검', '장애점검', '긴급점검')),

    constraint chk_inspection_result
        check (inspection_result in ('진행중', '정상', '조치필요', '교체필요'))
);

comment on table inspection is '충전기 점검 정보를 저장하는 테이블';

create index if not exists idx_inspection_fault on inspection(fault_id);
create index if not exists idx_inspection_charger on inspection(charger_id);
create index if not exists idx_inspection_inspector on inspection(inspector_id);
create index if not exists idx_inspection_date on inspection(inspection_date desc);

------------------------------------------------------------------------------------------------------------------------
-- 12. 전자결재 문서
------------------------------------------------------------------------------------------------------------------------
create table if not exists approval_document (
    document_id bigint generated always as identity primary key,
    writer_id bigint not null,
    fault_id bigint,
    inspection_id bigint,

    document_type varchar(50) not null,
    title varchar(200) not null,
    content text not null,
    estimated_cost numeric(12,2),
    status varchar(30) not null default '상신',

    created_at timestamp not null default current_timestamp,
    updated_at timestamp,
    completed_at timestamp,

    constraint fk_approval_writer
        foreign key (writer_id)
        references employee(employee_id),

    constraint fk_approval_fault
        foreign key (fault_id)
        references fault_report(fault_id),

    constraint fk_approval_inspection
        foreign key (inspection_id)
        references inspection(inspection_id),

    constraint chk_approval_status
        check (status in ('임시저장', '상신', '1차승인', '최종승인', '반려', '완료', '취소'))
);

comment on table approval_document is '전자결재 문서 정보를 저장하는 테이블';

create index if not exists idx_approval_status on approval_document(status);
create index if not exists idx_approval_writer on approval_document(writer_id);
create index if not exists idx_approval_created_at on approval_document(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 13. 전자결재 결재선
------------------------------------------------------------------------------------------------------------------------
create table if not exists approval_line (
    line_id bigint generated always as identity primary key,
    document_id bigint not null,
    approver_id bigint not null,
    approval_order int not null,
    status varchar(30) not null default '대기',
    comment text,
    approved_at timestamp,

    constraint fk_approval_line_document
        foreign key (document_id)
        references approval_document(document_id)
        on delete cascade,

    constraint fk_approval_line_employee
        foreign key (approver_id)
        references employee(employee_id),

    constraint uq_approval_line_order
        unique (document_id, approval_order),

    constraint chk_approval_line_status
        check (status in ('대기', '승인', '반려', '건너뜀'))
);

comment on table approval_line is '전자결재 결재선 정보를 저장하는 테이블';

create index if not exists idx_approval_line_document on approval_line(document_id);
create index if not exists idx_approval_line_approver on approval_line(approver_id);
create index if not exists idx_approval_line_status on approval_line(status);

------------------------------------------------------------------------------------------------------------------------
-- 14. 전자결재 처리 이력
------------------------------------------------------------------------------------------------------------------------
create table if not exists approval_history (
    history_id bigint generated always as identity primary key,
    document_id bigint not null,
    employee_id bigint not null,
    action_type varchar(30) not null,
    before_status varchar(30),
    after_status varchar(30),
    comment text,
    created_at timestamp not null default current_timestamp,

    constraint fk_approval_history_document
        foreign key (document_id)
        references approval_document(document_id)
        on delete cascade,

    constraint fk_approval_history_employee
        foreign key (employee_id)
        references employee(employee_id)
);

comment on table approval_history is '전자결재 처리 이력을 저장하는 테이블';

create index if not exists idx_approval_history_document on approval_history(document_id);
create index if not exists idx_approval_history_created_at on approval_history(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 15. 장애 조치 / 유지보수 작업
------------------------------------------------------------------------------------------------------------------------
create table if not exists maintenance_action (
    action_id bigint generated always as identity primary key,
    fault_id bigint not null,
    inspection_id bigint,
    approval_document_id bigint,
    charger_id bigint not null,
    employee_id bigint not null,

    action_type varchar(50) not null,
    action_result text,
    status varchar(30) not null default '진행중',
    started_at timestamp not null default current_timestamp,
    completed_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_action_fault
        foreign key (fault_id)
        references fault_report(fault_id),

    constraint fk_action_inspection
        foreign key (inspection_id)
        references inspection(inspection_id),

    constraint fk_action_approval
        foreign key (approval_document_id)
        references approval_document(document_id),

    constraint fk_action_charger
        foreign key (charger_id)
        references charger(charger_id),

    constraint fk_action_employee
        foreign key (employee_id)
        references employee(employee_id),

    constraint chk_action_status
        check (status in ('진행중', '완료', '중단'))
);

comment on table maintenance_action is '장애 조치 및 수리 작업 정보를 저장하는 테이블';

create index if not exists idx_action_fault on maintenance_action(fault_id);
create index if not exists idx_action_employee on maintenance_action(employee_id);
create index if not exists idx_action_status on maintenance_action(status);

------------------------------------------------------------------------------------------------------------------------
-- 16. 예약 대기열
-- 예약 고도화 기능 확장용
------------------------------------------------------------------------------------------------------------------------
create table if not exists reservation_waitlist (
    waitlist_id bigint generated always as identity primary key,
    member_id bigint not null,
    vehicle_id bigint not null,
    charger_id bigint not null,
    desired_start_time timestamp not null,
    desired_end_time timestamp not null,
    current_soc int not null,
    target_soc int not null,
    status varchar(30) not null default '대기',
    priority_score int not null default 0,
    created_at timestamp not null default current_timestamp,
    matched_at timestamp,
    canceled_at timestamp,

    constraint fk_waitlist_member
        foreign key (member_id)
        references app_member(member_id)
        on delete cascade,

    constraint fk_waitlist_vehicle
        foreign key (vehicle_id)
        references vehicle(vehicle_id)
        on delete cascade,

    constraint fk_waitlist_charger
        foreign key (charger_id)
        references charger(charger_id)
        on delete cascade,

    constraint chk_waitlist_status
        check (status in ('대기', '매칭완료', '취소', '만료')),

    constraint chk_waitlist_soc
        check (current_soc >= 0 and current_soc <= 100 and target_soc >= 0 and target_soc <= 100 and target_soc > current_soc),

    constraint chk_waitlist_time
        check (desired_end_time > desired_start_time)
);

comment on table reservation_waitlist is '충전 예약 대기열 정보를 저장하는 테이블';

create index if not exists idx_waitlist_charger_time on reservation_waitlist(charger_id, desired_start_time, desired_end_time);
create index if not exists idx_waitlist_member on reservation_waitlist(member_id);
create index if not exists idx_waitlist_status on reservation_waitlist(status);

------------------------------------------------------------------------------------------------------------------------
-- 17. 예약 상태 변경 이력
------------------------------------------------------------------------------------------------------------------------
create table if not exists reservation_history (
    history_id bigint generated always as identity primary key,
    reservation_id bigint not null,
    member_id bigint,
    employee_id bigint,
    before_status varchar(30),
    after_status varchar(30) not null,
    memo text,
    created_at timestamp not null default current_timestamp,

    constraint fk_reservation_history_reservation
        foreign key (reservation_id)
        references reservation(reservation_id)
        on delete cascade,

    constraint fk_reservation_history_member
        foreign key (member_id)
        references app_member(member_id),

    constraint fk_reservation_history_employee
        foreign key (employee_id)
        references employee(employee_id)
);

comment on table reservation_history is '예약 상태 변경 이력을 저장하는 테이블';

create index if not exists idx_reservation_history_reservation on reservation_history(reservation_id);
create index if not exists idx_reservation_history_created_at on reservation_history(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 18. 알림 정보
-- 사용자/관리자 공통 알림
------------------------------------------------------------------------------------------------------------------------
create table if not exists notification (
    notification_id bigint generated always as identity primary key,
    receiver_member_id bigint,
    receiver_employee_id bigint,
    notification_type varchar(50) not null,
    title varchar(200) not null,
    message text not null,
    target_url varchar(500),
    is_read boolean not null default false,
    created_at timestamp not null default current_timestamp,
    read_at timestamp,

    constraint fk_notification_member
        foreign key (receiver_member_id)
        references app_member(member_id),

    constraint fk_notification_employee
        foreign key (receiver_employee_id)
        references employee(employee_id),

    constraint chk_notification_receiver
        check (receiver_member_id is not null or receiver_employee_id is not null)
);

comment on table notification is '사용자 및 관리자 알림 정보를 저장하는 테이블';

create index if not exists idx_notification_member on notification(receiver_member_id, is_read);
create index if not exists idx_notification_employee on notification(receiver_employee_id, is_read);
create index if not exists idx_notification_created_at on notification(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 19. 첨부파일 공통 테이블
-- 공지사항, 민원, 점검, 결재 문서 첨부파일 확장용
------------------------------------------------------------------------------------------------------------------------
create table if not exists file_attachment (
    attachment_id bigint generated always as identity primary key,
    target_type varchar(50) not null,
    target_id bigint not null,
    original_file_name varchar(255) not null,
    stored_file_name varchar(255) not null,
    file_path varchar(500) not null,
    file_size bigint not null default 0,
    content_type varchar(100),
    uploader_member_id bigint,
    uploader_employee_id bigint,
    created_at timestamp not null default current_timestamp,

    constraint fk_attachment_member
        foreign key (uploader_member_id)
        references app_member(member_id),

    constraint fk_attachment_employee
        foreign key (uploader_employee_id)
        references employee(employee_id),

    constraint chk_attachment_target_type
        check (target_type in ('NOTICE', 'COMPLAINT', 'FAULT', 'INSPECTION', 'APPROVAL', 'MAINTENANCE'))
);

comment on table file_attachment is '공지사항, 민원, 점검, 결재 첨부파일을 저장하는 공통 테이블';

create index if not exists idx_attachment_target on file_attachment(target_type, target_id);

------------------------------------------------------------------------------------------------------------------------
-- 20. 환경부/외부 API 동기화 로그
------------------------------------------------------------------------------------------------------------------------
create table if not exists public_api_sync_log (
    sync_id bigint generated always as identity primary key,
    api_name varchar(100) not null,
    sync_type varchar(50) not null,
    status varchar(30) not null default '진행중',
    request_count int not null default 0,
    success_count int not null default 0,
    fail_count int not null default 0,
    error_message text,
    started_at timestamp not null default current_timestamp,
    finished_at timestamp,

    constraint chk_public_api_sync_status
        check (status in ('진행중', '성공', '실패', '부분성공'))
);

comment on table public_api_sync_log is '환경부 충전소 API 등 외부 데이터 동기화 로그';

create index if not exists idx_api_sync_log_api on public_api_sync_log(api_name);
create index if not exists idx_api_sync_log_started_at on public_api_sync_log(started_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 21. 관리자 대시보드 KPI 캐시/스냅샷
-- Redis 캐시와 별도로 일/월 단위 통계 스냅샷 저장 가능
------------------------------------------------------------------------------------------------------------------------
create table if not exists dashboard_kpi_snapshot (
    snapshot_id bigint generated always as identity primary key,
    snapshot_date date not null,
    total_members int not null default 0,
    today_reservations int not null default 0,
    active_charging_count int not null default 0,
    monthly_kwh numeric(12,2) not null default 0,
    monthly_sales numeric(14,2) not null default 0,
    pending_complaints int not null default 0,
    active_faults int not null default 0,
    pending_approvals int not null default 0,
    active_inspections int not null default 0,
    created_at timestamp not null default current_timestamp,

    constraint uq_dashboard_kpi_snapshot_date
        unique (snapshot_date)
);

comment on table dashboard_kpi_snapshot is '관리자 대시보드 KPI 일별 스냅샷 테이블';

------------------------------------------------------------------------------------------------------------------------
-- 22. 시스템 감사 로그
-- 관리자 화면에서 주요 처리 기록 추적
------------------------------------------------------------------------------------------------------------------------
create table if not exists system_audit_log (
    audit_id bigint generated always as identity primary key,
    actor_member_id bigint,
    actor_employee_id bigint,
    action_type varchar(100) not null,
    target_type varchar(100),
    target_id bigint,
    description text,
    ip_address varchar(50),
    created_at timestamp not null default current_timestamp,

    constraint fk_audit_member
        foreign key (actor_member_id)
        references app_member(member_id),

    constraint fk_audit_employee
        foreign key (actor_employee_id)
        references employee(employee_id)
);

comment on table system_audit_log is '관리자 및 시스템 주요 작업 감사 로그';

create index if not exists idx_audit_actor_member on system_audit_log(actor_member_id);
create index if not exists idx_audit_actor_employee on system_audit_log(actor_employee_id);
create index if not exists idx_audit_target on system_audit_log(target_type, target_id);
create index if not exists idx_audit_created_at on system_audit_log(created_at desc);

------------------------------------------------------------------------------------------------------------------------
-- 23. 기본 부서 데이터
-- 중복 실행해도 안전하게 처리
------------------------------------------------------------------------------------------------------------------------
insert into department (department_name, department_code, description)
values
    ('운영관리팀', 'OPS', '예약, 회원, 민원 운영 관리'),
    ('시설관리팀', 'FACILITY', '충전기 장애, 점검, 유지보수 관리'),
    ('경영관리팀', 'MANAGEMENT', '전자결재, 예산, 통계 관리'),
    ('시스템관리팀', 'SYSTEM', '권한, 시스템 설정, 데이터 연계 관리')
on conflict (department_code) do nothing;

------------------------------------------------------------------------------------------------------------------------
-- 24. 기본 FAQ 데이터
------------------------------------------------------------------------------------------------------------------------
insert into faq (category, question, answer, display_order)
values
    ('예약', '예약한 충전기가 사용 중이면 어떻게 하나요?', '예약 시간에 충전기가 사용 중이거나 장애 상태이면 고객센터 민원 접수 또는 관리자 확인을 통해 조치할 수 있습니다.', 1),
    ('장애', '충전소 장애를 신고할 수 있나요?', '충전소 찾기 또는 민원 접수 화면에서 충전소명과 충전기명을 입력하여 장애를 신고할 수 있습니다.', 2),
    ('민원', '민원 처리 결과는 어디서 확인하나요?', '마이페이지 또는 고객센터의 민원 내역 조회에서 처리 상태와 답변을 확인할 수 있습니다.', 3),
    ('예약', '충전 예약 취소는 어떻게 하나요?', '내 예약 조회 화면에서 예약 상태가 완료 전인 경우 예약 취소가 가능합니다.', 4),
    ('결제', '결제 오류가 발생했어요.', '결제 오류가 발생한 경우 민원 유형을 결제문의로 선택하여 접수하면 운영 담당자가 확인합니다.', 5)
on conflict do nothing;

------------------------------------------------------------------------------------------------------------------------
-- 25. 적용 확인용 조회
------------------------------------------------------------------------------------------------------------------------
-- 아래 SELECT는 실행 결과 확인용입니다.
select 'EV Charge MIS 확장 SQL 적용 완료' as result_message;

/* =========================================================
   PART 3. 생성 테이블 확인 쿼리
========================================================= */

select table_name
from information_schema.tables
where table_schema = 'public'
order by table_name;



/* =========================================================
   SECTION 2. 공지사항 게시판/기본 공지 데이터
   원본 파일: notice_schema_seed.sql
========================================================= */

/* =========================================================
   EV Charge MIS 공지사항 게시판 SQL

   사용 위치
   - PostgreSQL 16 / ev_charge_mis DB
   - DBeaver에서 실행

   목적
   - notice 테이블이 없을 경우 생성
   - 프론트 공지사항 화면 확인용 기본 공지 3건 등록
========================================================= */

create table if not exists notice (
    notice_id bigint generated always as identity primary key,
    writer_id bigint,
    title varchar(200) not null,
    content text not null,
    category varchar(30) not null default '공지',
    status varchar(20) not null default '게시',
    is_pinned boolean not null default false,
    is_public boolean not null default true,
    view_count int not null default 0,
    start_at timestamp,
    end_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,

    constraint fk_notice_writer
        foreign key (writer_id)
        references employee(employee_id),

    constraint chk_notice_status
        check (status in ('임시저장', '게시', '숨김', '삭제'))
);

comment on table notice is '공지사항 정보를 저장하는 테이블';
comment on column notice.category is '공지 카테고리: 공지, 점검, 안내 등';

create index if not exists idx_notice_status on notice(status);
create index if not exists idx_notice_public on notice(is_public);
create index if not exists idx_notice_pinned on notice(is_pinned);
create index if not exists idx_notice_created_at on notice(created_at desc);

insert into notice (title, content, category, status, is_pinned, is_public, view_count, created_at, updated_at)
select
    '공공 전기차 충전 인프라 운영 플랫폼 시범 운영 안내',
    '공공 전기차 충전 인프라 운영 플랫폼 시범 운영을 안내드립니다.' || chr(10) || chr(10) ||
    '이번 시범 운영 기간 동안 충전소 찾기, 충전 예약, 차량 관리, 민원 접수 기능을 이용할 수 있습니다.' || chr(10) || chr(10) ||
    '서비스 이용 중 불편사항이 발생할 경우 고객센터의 민원 접수를 통해 문의해 주시기 바랍니다.',
    '공지',
    '게시',
    true,
    true,
    128,
    current_timestamp - interval '6 day',
    current_timestamp - interval '6 day'
where not exists (
    select 1 from notice where title = '공공 전기차 충전 인프라 운영 플랫폼 시범 운영 안내'
);

insert into notice (title, content, category, status, is_pinned, is_public, view_count, created_at, updated_at)
select
    '부산시청 충전소 정기 점검 안내',
    '부산시청 충전소 정기 점검 일정을 안내드립니다.' || chr(10) || chr(10) ||
    '점검 시간 동안 일부 충전기 이용이 제한될 수 있습니다.' || chr(10) || chr(10) ||
    '이용자는 주변 충전소를 확인한 후 예약해 주시기 바랍니다.',
    '공지',
    '게시',
    false,
    true,
    64,
    current_timestamp - interval '5 day',
    current_timestamp - interval '5 day'
where not exists (
    select 1 from notice where title = '부산시청 충전소 정기 점검 안내'
);

insert into notice (title, content, category, status, is_pinned, is_public, view_count, created_at, updated_at)
select
    '예약 노쇼 방지 캠페인 안내',
    '예약 후 미사용을 줄이기 위한 노쇼 방지 캠페인을 진행합니다.' || chr(10) || chr(10) ||
    '예약 시간에 이용이 어려운 경우 반드시 예약을 취소해 주시기 바랍니다.' || chr(10) || chr(10) ||
    '올바른 예약 문화 정착을 위해 이용자 여러분의 협조를 부탁드립니다.',
    '공지',
    '게시',
    false,
    true,
    51,
    current_timestamp - interval '4 day',
    current_timestamp - interval '4 day'
where not exists (
    select 1 from notice where title = '예약 노쇼 방지 캠페인 안내'
);

select '공지사항 게시판 SQL 적용 완료' as result_message;


/* =========================================================
   SECTION 3. AI 채팅 메시지 카드 메타데이터 컬럼
   원본 파일: AI 채팅 메시지 카드 데이터 유지용 컬럼 추가.sql
========================================================= */

/* =========================================================
   AI 채팅 메시지 카드 데이터 유지용 컬럼 추가

   목적
   - AI 채팅 이력을 다시 조회해도 actionType / candidates / reservations 유지
   - 지도/다른 화면 이동 후 돌아와도 차량등록/예약상세/내역보기 버튼 유지

   실행 위치
   - PostgreSQL / DBeaver에서 ev_charge_mis DB 선택 후 실행
========================================================= */

alter table ai_chat_message
    add column if not exists intent varchar(80),
    add column if not exists action_type varchar(80),
    add column if not exists button_text varchar(100),
    add column if not exists action_url varchar(300),
    add column if not exists location_json text,
    add column if not exists candidates_json text,
    add column if not exists reservations_json text;

comment on column ai_chat_message.intent is 'AI 응답 의도값: DEFAULT_VEHICLE, MY_RESERVATION_LIST 등';
comment on column ai_chat_message.action_type is '프론트 액션 버튼 타입';
comment on column ai_chat_message.button_text is '프론트 액션 버튼 문구';
comment on column ai_chat_message.action_url is '프론트 이동 URL';
comment on column ai_chat_message.location_json is '지도 이동용 위치 데이터 JSON';
comment on column ai_chat_message.candidates_json is 'AI 예약 후보 카드 데이터 JSON';
comment on column ai_chat_message.reservations_json is '내 예약 카드 데이터 JSON';

select 'AI 채팅 메시지 메타데이터 컬럼 추가 완료' as result_message;


/* =========================================================
   SECTION 4. 전자결재 4차 상신 흐름 보강
   원본 파일: 4차 전자결재 상신 흐름 보강 SQL.sql
========================================================= */

/* =========================================================
   4차 전자결재 상신 흐름 보강 SQL
   - approval_document / approval_line / approval_history 생성
   - 결재선 서명 저장 컬럼 사전 보강
   - 시연용 관리자 직원 데이터 보강
========================================================= */

create table if not exists approval_document (
    document_id bigint generated always as identity primary key,
    writer_id bigint not null,
    fault_id bigint,
    inspection_id bigint,
    document_type varchar(50) not null,
    title varchar(200) not null,
    content text not null,
    estimated_cost numeric(12,2),
    status varchar(30) not null default '상신',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,
    completed_at timestamp,
    constraint fk_approval_writer foreign key (writer_id) references employee(employee_id),
    constraint fk_approval_fault foreign key (fault_id) references fault_report(fault_id),
    constraint fk_approval_inspection foreign key (inspection_id) references inspection(inspection_id),
    constraint chk_approval_status check (status in ('임시저장', '상신', '1차승인', '최종승인', '반려', '완료', '취소'))
);

create index if not exists idx_approval_status on approval_document(status);
create index if not exists idx_approval_writer on approval_document(writer_id);
create index if not exists idx_approval_fault on approval_document(fault_id);
create index if not exists idx_approval_created_at on approval_document(created_at desc);

create table if not exists approval_line (
    line_id bigint generated always as identity primary key,
    document_id bigint not null,
    approver_id bigint not null,
    approval_order int not null,
    status varchar(30) not null default '대기',
    comment text,
    signature_data text,
    approved_at timestamp,
    constraint fk_approval_line_document foreign key (document_id) references approval_document(document_id) on delete cascade,
    constraint fk_approval_line_employee foreign key (approver_id) references employee(employee_id),
    constraint uq_approval_line_order unique (document_id, approval_order),
    constraint chk_approval_line_status check (status in ('대기', '승인', '반려', '건너뜀'))
);

alter table approval_line
    add column if not exists signature_data text;

create index if not exists idx_approval_line_document on approval_line(document_id);
create index if not exists idx_approval_line_approver on approval_line(approver_id);
create index if not exists idx_approval_line_status on approval_line(status);

create table if not exists approval_history (
    history_id bigint generated always as identity primary key,
    document_id bigint not null,
    employee_id bigint not null,
    action_type varchar(30) not null,
    before_status varchar(30),
    after_status varchar(30),
    comment text,
    created_at timestamp not null default current_timestamp,
    constraint fk_approval_history_document foreign key (document_id) references approval_document(document_id) on delete cascade,
    constraint fk_approval_history_employee foreign key (employee_id) references employee(employee_id)
);

create index if not exists idx_approval_history_document on approval_history(document_id);
create index if not exists idx_approval_history_created_at on approval_history(created_at desc);

-- 시연용 계정이 app_member에는 있으나 employee에 누락된 경우 보강
insert into employee (
      member_id
    , department_id
    , employee_no
    , position_name
    , duty_name
    , status
    , hired_at
    , created_at
    , updated_at
)
select
      m.member_id
    , case
          when m.user_id = 'admin' then (select department_id from department where department_code = 'SYSTEM')
          when m.user_id = 'manager' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'operator' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'engineer' then (select department_id from department where department_code = 'FACILITY')
      end
    , case
          when m.user_id = 'admin' then 'EMP-ADMIN-001'
          when m.user_id = 'manager' then 'EMP-MANAGER-001'
          when m.user_id = 'operator' then 'EMP-OPERATOR-001'
          when m.user_id = 'engineer' then 'EMP-ENGINEER-001'
      end
    , case
          when m.user_id = 'admin' then '기관장'
          when m.user_id = 'manager' then '운영관리자'
          when m.user_id = 'operator' then '운영담당자'
          when m.user_id = 'engineer' then '시설관리담당자'
      end
    , case
          when m.user_id = 'admin' then '권한 및 시스템 총괄'
          when m.user_id = 'manager' then '운영 업무 배정 및 1차 승인'
          when m.user_id = 'operator' then '민원 및 예약 운영'
          when m.user_id = 'engineer' then '충전기 장애 및 점검 처리'
      end
    , 'ACTIVE'
    , current_date
    , current_timestamp
    , current_timestamp
from app_member m
where m.user_id in ('admin', 'manager', 'operator', 'engineer')
  and not exists (
      select 1
        from employee e
       where e.member_id = m.member_id
  );

update employee e
   set status = 'ACTIVE',
       updated_at = current_timestamp
  from app_member m
 where e.member_id = m.member_id
   and m.user_id in ('admin', 'manager', 'operator', 'engineer');

select '전자결재 상신 흐름 보강 완료' as result_message;


/* =========================================================
   SECTION 5. 전자결재 5차 승인/서명 보강
   원본 파일: 전자결재 5차 패치.sql
========================================================= */

/* =========================================================
   전자결재 5차 패치
   - 결재선 전자서명 저장 컬럼 보강
   - 시연용 직원 데이터 보강
   적용 대상: PostgreSQL ev_charge_mis
========================================================= */

alter table approval_line
    add column if not exists signature_data text;

-- 기존 스키마에는 approved_at만 있어도 충분하지만,
-- 일부 환경에서 signed_at 컬럼을 별도 조회할 수 있도록 선택 보강합니다.
alter table approval_line
    add column if not exists signed_at timestamp;

-- 승인 시 approved_at을 기준으로 사용하되 signed_at도 같은 의미로 활용할 수 있습니다.

-- 시연 계정이 app_member에만 있고 employee에 없는 경우 보강
insert into employee (
      member_id
    , department_id
    , employee_no
    , position_name
    , duty_name
    , status
    , hired_at
    , created_at
    , updated_at
)
select
      m.member_id
    , case
          when m.user_id = 'admin' then (select department_id from department where department_code = 'SYSTEM')
          when m.user_id = 'manager' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'operator' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'engineer' then (select department_id from department where department_code = 'FACILITY')
      end as department_id
    , case
          when m.user_id = 'admin' then 'EMP-ADMIN-001'
          when m.user_id = 'manager' then 'EMP-MANAGER-001'
          when m.user_id = 'operator' then 'EMP-OPERATOR-001'
          when m.user_id = 'engineer' then 'EMP-ENGINEER-001'
      end as employee_no
    , case
          when m.user_id = 'admin' then '기관장'
          when m.user_id = 'manager' then '운영관리자'
          when m.user_id = 'operator' then '운영담당자'
          when m.user_id = 'engineer' then '시설관리담당자'
      end as position_name
    , case
          when m.user_id = 'admin' then '권한 및 시스템 총괄'
          when m.user_id = 'manager' then '운영 업무 배정 및 1차 승인'
          when m.user_id = 'operator' then '민원 및 예약 운영'
          when m.user_id = 'engineer' then '충전기 장애 및 점검 처리'
      end as duty_name
    , 'ACTIVE'
    , current_date
    , current_timestamp
    , current_timestamp
from app_member m
where m.user_id in ('admin', 'manager', 'operator', 'engineer')
  and not exists (
      select 1
        from employee e
       where e.member_id = m.member_id
  );

update employee e
   set status = 'ACTIVE',
       updated_at = current_timestamp
  from app_member m
 where e.member_id = m.member_id
   and m.user_id in ('admin', 'manager', 'operator', 'engineer');

select '전자결재 승인/반려/서명 5차 패치 적용 완료' as result_message;


/* =========================================================
   SECTION 6. 6차 직원 계정/내 정보 관리 보강
   원본 파일: 6차 작업 직원 계정내 정보 관리 보강 SQL.sql
========================================================= */

/* =========================================================
   6차 작업: 직원 계정/내 정보 관리 보강 SQL
   - 시연용 관리자 계정이 app_member에는 있지만 employee에 없는 경우 보정
   - 직원 등록은 app_member + employee 1:1 구조로 운영
   - 비밀번호 변경/초기화는 기존 app_member.password 컬럼을 사용
========================================================= */

-- 직원 권한 CHECK 확장 보강
alter table app_member
    drop constraint if exists chk_member_user_type;

alter table app_member
    add constraint chk_member_user_type
    check (user_type in ('USER', 'ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'));

-- 기본 부서 보강
insert into department (department_name, department_code, description, is_active)
values
    ('운영관리팀', 'OPS', '예약, 회원, 민원 운영 관리', true),
    ('시설관리팀', 'FACILITY', '충전기 장애, 점검, 유지보수 관리', true),
    ('경영관리팀', 'MANAGEMENT', '전자결재, 예산, 통계 관리', true),
    ('시스템관리팀', 'SYSTEM', '권한, 시스템 설정, 데이터 연계 관리', true)
on conflict (department_code) do update set
      department_name = excluded.department_name
    , description = excluded.description
    , is_active = true;

-- 시연용 계정이 있는 경우 employee 1:1 직원정보 연결 보강
insert into employee (
      member_id
    , department_id
    , employee_no
    , position_name
    , duty_name
    , status
    , hired_at
    , created_at
    , updated_at
)
select
      m.member_id
    , case
          when m.user_id = 'admin' then (select department_id from department where department_code = 'SYSTEM')
          when m.user_id = 'manager' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'operator' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'engineer' then (select department_id from department where department_code = 'FACILITY')
      end as department_id
    , case
          when m.user_id = 'admin' then 'EMP-ADMIN-001'
          when m.user_id = 'manager' then 'EMP-MANAGER-001'
          when m.user_id = 'operator' then 'EMP-OPERATOR-001'
          when m.user_id = 'engineer' then 'EMP-ENGINEER-001'
      end as employee_no
    , case
          when m.user_id = 'admin' then '기관장'
          when m.user_id = 'manager' then '운영관리자'
          when m.user_id = 'operator' then '운영담당자'
          when m.user_id = 'engineer' then '시설관리담당자'
      end as position_name
    , case
          when m.user_id = 'admin' then '권한 및 시스템 총괄'
          when m.user_id = 'manager' then '운영 업무 배정 및 1차 승인'
          when m.user_id = 'operator' then '민원 및 예약 운영'
          when m.user_id = 'engineer' then '충전기 장애 및 점검 처리'
      end as duty_name
    , 'ACTIVE'
    , current_date
    , current_timestamp
    , current_timestamp
from app_member m
where m.user_id in ('admin', 'manager', 'operator', 'engineer')
  and not exists (
      select 1
        from employee e
       where e.member_id = m.member_id
  );

-- 이미 직원정보가 있는 시연 계정은 ACTIVE로 보정
update employee e
   set status = 'ACTIVE',
       updated_at = current_timestamp
  from app_member m
 where e.member_id = m.member_id
   and m.user_id in ('admin', 'manager', 'operator', 'engineer');

update app_member
   set status = 'ACTIVE',
       updated_at = current_timestamp
 where user_id in ('admin', 'manager', 'operator', 'engineer');

select '직원 계정/내 정보 관리 6차 보강 SQL 적용 완료' as result_message;


/* =========================================================
   SECTION 7. 중복 실행 안전 보강 제약조건
   - 기본 스키마 마지막 UNIQUE 제약조건을 DO 블록으로 정리
   - 같은 SQL을 다시 실행해도 constraint already exists 오류 방지
========================================================= */

do $$
begin
    if not exists (
        select 1
          from pg_constraint
         where conname = 'uq_charging_station_external_station_id'
    ) then
        alter table charging_station
            add constraint uq_charging_station_external_station_id
            unique (external_station_id);
    end if;
end $$;

do $$
begin
    if not exists (
        select 1
          from pg_constraint
         where conname = 'uq_charger_external_charger_id'
    ) then
        alter table charger
            add constraint uq_charger_external_charger_id
            unique (external_charger_id);
    end if;
end $$;

/* =========================================================
   SECTION 8. 최종 적용 확인
========================================================= */

select 'EV Charge MIS 최종 통합 DB 생성 SQL 적용 완료' as result_message;

select table_name
from information_schema.tables
where table_schema = 'public'
order by table_name;
