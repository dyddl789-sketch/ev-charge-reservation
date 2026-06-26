/* =========================================================
   장애·점검 전자결재 상태 보정 SQL
   목적
   - 전자결재 최종승인 이후 fault_report가 결재대기/점검중에 남아
     조치 진행 버튼이 비활성화되는 기존 데이터 보정
   - 중복 실행해도 최대한 안전하도록 exists 조건 사용
========================================================= */

-- 1) 최종승인 문서가 있는데 진행중 조치 작업이 없는 경우 조치 작업 생성
insert into maintenance_action (
      fault_id
    , inspection_id
    , approval_document_id
    , charger_id
    , employee_id
    , action_type
    , action_result
    , status
    , started_at
    , created_at
    , updated_at
)
select
      f.fault_id
    , ad.inspection_id
    , ad.document_id
    , f.charger_id
    , coalesce(final_history.employee_id, ad.writer_id)
    , '교체작업'
    , '전자결재 최종 승인 후 교체 작업 진행 중'
    , '진행중'
    , current_timestamp
    , current_timestamp
    , current_timestamp
  from approval_document ad
  join fault_report f
    on ad.fault_id = f.fault_id
  left join lateral (
      select ah.employee_id
        from approval_history ah
       where ah.document_id = ad.document_id
         and ah.after_status = '최종승인'
       order by ah.created_at desc, ah.history_id desc
       limit 1
  ) final_history on true
 where ad.status = '최종승인'
   and f.status in ('결재대기', '점검중')
   and not exists (
       select 1
         from maintenance_action ma
        where ma.approval_document_id = ad.document_id
          and ma.status = '진행중'
   );

-- 2) 최종승인 문서와 진행중 조치 작업이 있으면 장애 상태를 조치중으로 복구
update fault_report f
   set status = '조치중',
       updated_at = current_timestamp,
       resolved_at = null
 where f.status in ('결재대기', '점검중')
   and exists (
       select 1
         from approval_document ad
        where ad.fault_id = f.fault_id
          and ad.status = '최종승인'
   )
   and exists (
       select 1
         from maintenance_action ma
        where ma.fault_id = f.fault_id
          and ma.status = '진행중'
   );

-- 3) 민원도 처리중 상태로 맞춤
update complaint c
   set status = '처리중',
       admin_memo = '전자결재 최종 승인으로 교체 작업이 진행됩니다.',
       updated_at = current_timestamp,
       closed_at = null
  from fault_report f
  join approval_document ad
    on ad.fault_id = f.fault_id
 where f.complaint_id = c.complaint_id
   and f.status = '조치중'
   and ad.status = '최종승인';
