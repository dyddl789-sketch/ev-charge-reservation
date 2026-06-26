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
