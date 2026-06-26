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
