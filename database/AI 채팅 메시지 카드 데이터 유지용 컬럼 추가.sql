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
