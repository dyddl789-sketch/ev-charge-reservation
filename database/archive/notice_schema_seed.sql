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
