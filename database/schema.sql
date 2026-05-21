-- PostGIS 확장 기능 활성화
-- geography(Point, 4326) 타입을 사용하기 위해 필요함
-- 충전소와 사용자 위치의 거리 계산, 주변 충전소 검색 등에 사용
create extension if not exists postgis;

------------------------------------------------------------------------------------------------------------------------
-- 회원 정보
create table app_member (
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
create table saved_location (
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
create table vehicle_model (
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

------------------------------------------------------------------------------------------------------------------------
-- 사용자 등록 차량
create table vehicle (
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

------------------------------------------------------------------------------------------------------------------------
-- 충전소
create table charging_station (
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
create table charger (
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
create table reservation (
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
create index idx_reservation_charger_time
on reservation(charger_id, start_time, end_time);

------------------------------------------------------------------------------------------------------------------------
-- 충전 세션
create table charging_session (
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
create index idx_charging_session_member
on charging_session(member_id);

create index idx_charging_session_charger
on charging_session(charger_id);

------------------------------------------------------------------------------------------------------------------------
-- 즐겨찾기
create table favorite_station (
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
create table ai_chat_room (
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
create table ai_chat_message (
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
