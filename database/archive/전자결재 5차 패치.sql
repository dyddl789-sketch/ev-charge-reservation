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
