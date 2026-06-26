/* =========================================================
   MIS 대시보드/권한/직원관리 시연용 기본 데이터
   적용 대상: PostgreSQL ev_charge_mis

   기본 비밀번호: 1234
   계정:
   - admin / 1234
   - manager / 1234
   - operator / 1234
   - engineer / 1234
========================================================= */

-- 권한 CHECK 확장
alter table app_member
    drop constraint if exists chk_member_user_type;

alter table app_member
    add constraint chk_member_user_type
    check (user_type in ('USER', 'ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'));

-- 기본 부서
insert into department (department_name, department_code, description)
values
    ('운영관리팀', 'OPS', '예약, 회원, 민원 운영 관리'),
    ('시설관리팀', 'FACILITY', '충전기 장애, 점검, 유지보수 관리'),
    ('경영관리팀', 'MANAGEMENT', '전자결재, 예산, 통계 관리'),
    ('시스템관리팀', 'SYSTEM', '권한, 시스템 설정, 데이터 연계 관리')
on conflict (department_code) do nothing;


-- 공공데이터 upsert용 UNIQUE 제약 보강
-- 이미 같은 제약이 있으면 생략합니다.
do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'uq_charging_station_external_station_id'
    ) then
        alter table charging_station
            add constraint uq_charging_station_external_station_id
            unique (external_station_id);
    end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'uq_charger_external_charger_id'
    ) then
        alter table charger
            add constraint uq_charger_external_charger_id
            unique (external_charger_id);
    end if;
end $$;

-- 기본 관리자/직원 계정
-- BCrypt(1234)
with upsert_member as (
    insert into app_member (
          user_id
        , password
        , member_name
        , nickname
        , phone
        , email
        , user_type
        , login_type
        , status
        , created_at
        , updated_at
    ) values
        ('admin', '$2y$10$nWxp/aqFr3OMjmfA/GNbuex7hBw9zJMndx77fn7IQXFL2ORa5od/G', '최고관리자', '최고관리자', null, 'admin@ev-mis.go.kr', 'ADMIN', 'LOCAL', 'ACTIVE', current_timestamp, current_timestamp),
        ('manager', '$2y$10$nWxp/aqFr3OMjmfA/GNbuex7hBw9zJMndx77fn7IQXFL2ORa5od/G', '운영관리자', '운영관리자', null, 'manager@ev-mis.go.kr', 'MANAGER', 'LOCAL', 'ACTIVE', current_timestamp, current_timestamp),
        ('operator', '$2y$10$nWxp/aqFr3OMjmfA/GNbuex7hBw9zJMndx77fn7IQXFL2ORa5od/G', '운영담당자', '운영담당자', null, 'operator@ev-mis.go.kr', 'OPERATOR', 'LOCAL', 'ACTIVE', current_timestamp, current_timestamp),
        ('engineer', '$2y$10$nWxp/aqFr3OMjmfA/GNbuex7hBw9zJMndx77fn7IQXFL2ORa5od/G', '시설관리담당자', '시설관리담당자', null, 'engineer@ev-mis.go.kr', 'ENGINEER', 'LOCAL', 'ACTIVE', current_timestamp, current_timestamp)
    on conflict (user_id)
    do update set
          user_type = excluded.user_type
        , status = 'ACTIVE'
        , updated_at = current_timestamp
    returning member_id, user_id
)
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
on conflict (member_id) do update set
      department_id = excluded.department_id
    , position_name = excluded.position_name
    , duty_name = excluded.duty_name
    , status = 'ACTIVE'
    , updated_at = current_timestamp;

select 'MIS 권한/직원 시연용 계정 적용 완료' as result_message;


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
          when m.user_id = 'manager' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'operator' then (select department_id from department where department_code = 'OPS')
          when m.user_id = 'engineer' then (select department_id from department where department_code = 'FACILITY')
      end as department_id
    , case
          when m.user_id = 'manager' then 'EMP-MANAGER-001'
          when m.user_id = 'operator' then 'EMP-OPERATOR-001'
          when m.user_id = 'engineer' then 'EMP-ENGINEER-001'
      end as employee_no
    , case
          when m.user_id = 'manager' then '운영관리자'
          when m.user_id = 'operator' then '운영담당자'
          when m.user_id = 'engineer' then '시설관리담당자'
      end as position_name
    , case
          when m.user_id = 'manager' then '운영 업무 배정 및 1차 승인'
          when m.user_id = 'operator' then '민원 및 예약 운영'
          when m.user_id = 'engineer' then '충전기 장애 및 점검 처리'
      end as duty_name
    , 'ACTIVE'
    , current_date
    , current_timestamp
    , current_timestamp
from app_member m
where m.user_id in ('manager', 'operator', 'engineer')
  and not exists (
      select 1
      from employee e
      where e.member_id = m.member_id
  );
  
  
  --확인용
  select
      e.employee_id
    , m.user_id
    , m.member_name
    , m.user_type
    , m.status as member_status
    , e.status as employee_status
    , d.department_code
    , d.department_name
from employee e
join app_member m
    on e.member_id = m.member_id
join department d
    on e.department_id = d.department_id
where e.status = 'ACTIVE'
  and m.status = 'ACTIVE'
  and m.user_type = 'ENGINEER';