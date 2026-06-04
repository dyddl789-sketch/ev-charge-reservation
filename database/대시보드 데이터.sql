-- =========================================================
-- EV Charge 관리자 시연용 풍부한 더미데이터
-- 전제:
-- 1. vehicle_model 이미 있음
-- 2. charging_station 10개 있음
-- 3. charger 30개 있음
--
-- 생성:
-- 회원 30명
-- 위치 30개
-- 차량 45대
-- 완료 예약/세션 189건
-- 현재 충전중 8건
-- 미래 예약완료 20건
-- 미래 인증완료 10건
-- 취소 6건
-- 노쇼 6건
--
-- 목적:
-- 4/3 ~ 6/4 매일 차트가 비지 않게 출력
-- 이용통계/매출통계/대시보드/예약관리 풍부하게 출력
-- =========================================================

begin;

-- 기존 ADM 더미 삭제
delete from charging_session
where reservation_id in (
    select reservation_id
    from reservation
    where auth_code like 'ADM%'
);

delete from reservation
where auth_code like 'ADM%';

delete from saved_location
where member_id in (
    select member_id
    from app_member
    where user_id like 'demo_user_%'
);

delete from vehicle
where member_id in (
    select member_id
    from app_member
    where user_id like 'demo_user_%'
);

delete from ai_chat_message
where room_id in (
    select room_id
    from ai_chat_room
    where member_id in (
        select member_id
        from app_member
        where user_id like 'demo_user_%'
    )
);

delete from ai_chat_room
where member_id in (
    select member_id
    from app_member
    where user_id like 'demo_user_%'
);

delete from app_member
where user_id like 'demo_user_%';


-- 회원 30명
insert into app_member (
      user_id
    , password
    , member_name
    , nickname
    , phone
    , email
    , profile_image_url
    , user_type
    , login_type
    , status
    , last_login_at
    , created_at
    , updated_at
)
select
      'demo_user_' || lpad(n::text, 2, '0')
    , '$2a$10$7aeqE0hZ2Fu/maj9BJwqBOCOrDINrpw54iN.7KPxHq6FvwmbnADxO'
    , '시연회원' || n
    , '데모회원' || n
    , '010-88' || lpad(n::text, 2, '0') || '-' || lpad((1000 + n)::text, 4, '0')
    , 'demo_user_' || lpad(n::text, 2, '0') || '@evcharge.test'
    , '/images/member/profile/default-profile.png'
    , 'USER'
    , case when n in (5, 10, 15, 20, 25, 30) then 'KAKAO' else 'LOCAL' end
    , case
        when n in (27, 28) then 'INACTIVE'
        when n in (29, 30) then 'BLOCKED'
        else 'ACTIVE'
      end
    , current_timestamp - (n * interval '2 hours')
    , current_date - ((n % 30) * interval '1 day')
    , current_timestamp
from generate_series(1, 30) gs(n);


-- 기본 위치 30개
with demo_member as (
    select row_number() over(order by member_id) as rn, member_id
    from app_member
    where user_id like 'demo_user_%'
)
insert into saved_location (
      member_id
    , location_name
    , location_type
    , address
    , latitude
    , longitude
    , location
    , is_default
    , created_at
)
select
      member_id
    , case when rn % 5 = 1 then '집'
           when rn % 5 = 2 then '회사'
           when rn % 5 = 3 then '학교'
           when rn % 5 = 4 then '자주 가는 곳'
           else '기본 위치' end
    , case when rn % 5 = 1 then 'HOME'
           when rn % 5 = 2 then 'WORK'
           when rn % 5 = 3 then 'SCHOOL'
           when rn % 5 = 4 then 'FAVORITE'
           else 'CUSTOM' end
    , case when rn % 10 = 1 then '부산광역시 부산진구 중앙대로 730'
           when rn % 10 = 2 then '부산광역시 해운대구 해운대해변로 264'
           when rn % 10 = 3 then '부산광역시 수영구 광안해변로 219'
           when rn % 10 = 4 then '부산광역시 동구 중앙대로 206'
           when rn % 10 = 5 then '부산광역시 사상구 광장로 83'
           when rn % 10 = 6 then '부산광역시 북구 금곡대로 14'
           when rn % 10 = 7 then '부산광역시 강서구 명지국제7로 37'
           when rn % 10 = 8 then '부산광역시 사하구 다대로 680'
           when rn % 10 = 9 then '부산광역시 중구 광복로 72'
           else '부산광역시 기장군 기장읍 기장대로 560' end
    , case when rn % 10 = 1 then 35.1578
           when rn % 10 = 2 then 35.1587
           when rn % 10 = 3 then 35.1532
           when rn % 10 = 4 then 35.1151
           when rn % 10 = 5 then 35.1628
           when rn % 10 = 6 then 35.2101
           when rn % 10 = 7 then 35.0955
           when rn % 10 = 8 then 35.0472
           when rn % 10 = 9 then 35.0983
           else 35.2446 end
    , case when rn % 10 = 1 then 129.0592
           when rn % 10 = 2 then 129.1604
           when rn % 10 = 3 then 129.1186
           when rn % 10 = 4 then 129.0415
           when rn % 10 = 5 then 128.9846
           when rn % 10 = 6 then 129.0080
           when rn % 10 = 7 then 128.9035
           when rn % 10 = 8 then 128.9684
           when rn % 10 = 9 then 129.0345
           else 129.2220 end
    , ST_SetSRID(
        ST_MakePoint(
            case when rn % 10 = 1 then 129.0592
                 when rn % 10 = 2 then 129.1604
                 when rn % 10 = 3 then 129.1186
                 when rn % 10 = 4 then 129.0415
                 when rn % 10 = 5 then 128.9846
                 when rn % 10 = 6 then 129.0080
                 when rn % 10 = 7 then 128.9035
                 when rn % 10 = 8 then 128.9684
                 when rn % 10 = 9 then 129.0345
                 else 129.2220 end,
            case when rn % 10 = 1 then 35.1578
                 when rn % 10 = 2 then 35.1587
                 when rn % 10 = 3 then 35.1532
                 when rn % 10 = 4 then 35.1151
                 when rn % 10 = 5 then 35.1628
                 when rn % 10 = 6 then 35.2101
                 when rn % 10 = 7 then 35.0955
                 when rn % 10 = 8 then 35.0472
                 when rn % 10 = 9 then 35.0983
                 else 35.2446 end
        ),
        4326
      )::geography
    , true
    , current_timestamp
from demo_member;


-- 차량 45대
with demo_member as (
    select row_number() over(order by member_id) as rn, member_id
    from app_member
    where user_id like 'demo_user_%'
),
model_pool as (
    select row_number() over(order by model_id) as rn,
           count(*) over() as total_count,
           model_id
    from vehicle_model
),
vehicle_source as (
    select gs.n, dm.member_id, mp.model_id
    from generate_series(1, 45) gs(n)
    join demo_member dm
      on dm.rn = ((gs.n - 1) % 30) + 1
    join model_pool mp
      on mp.rn = ((gs.n - 1) % mp.total_count) + 1
)
insert into vehicle (
      member_id
    , model_id
    , vehicle_nickname
    , plate_number
    , is_default
    , created_at
    , updated_at
    , is_deleted
)
select
      member_id
    , model_id
    , case when n % 3 = 1 then '출퇴근용'
           when n % 3 = 2 then '가족차'
           else '업무용' end || n
    , lpad((20 + n)::text, 2, '0') || '가' || lpad((2000 + n)::text, 4, '0')
    , case when n <= 30 then true else false end
    , current_timestamp - (n * interval '12 hours')
    , current_timestamp
    , false
from vehicle_source;

-- 기본차량 한 대만 유지
with ranked_vehicle as (
    select
          vehicle_id
        , member_id
        , row_number() over(
            partition by member_id
            order by is_default desc, vehicle_id asc
          ) as rn
    from vehicle
    where member_id in (
        select member_id
        from app_member
        where user_id like 'demo_user_%'
    )
      and is_deleted = false
)
update vehicle v
set is_default = case when rv.rn = 1 then true else false end,
    updated_at = current_timestamp
from ranked_vehicle rv
where v.vehicle_id = rv.vehicle_id;


-- =========================================================
-- 완료 예약/세션 약 250건
-- 2026-04-03 ~ 2026-06-04
-- 날짜별 1~7건으로 자연스럽게 분산
-- 금/토/월은 조금 더 많게 구성
-- 시간대도 오전/점심/오후/저녁으로 분산
-- =========================================================

with vehicle_pool as (
    select
          row_number() over(order by v.vehicle_id) as rn
        , count(*) over() as total_count
        , v.vehicle_id
        , v.member_id
        , vm.battery_capacity_kwh
    from vehicle v
    join vehicle_model vm
      on v.model_id = vm.model_id
    join app_member m
      on v.member_id = m.member_id
    where v.is_deleted = false
      and m.user_id like 'demo_user_%'
      and m.status = 'ACTIVE'
),
charger_pool as (
    select
          row_number() over(order by c.charger_id) as rn
        , count(*) over() as total_count
        , c.charger_id
        , c.charger_type
        , c.charging_speed_kw
        , c.price_per_kwh
    from charger c
    join charging_station st
      on c.station_id = st.station_id
    where st.station_status = '운영중'
),
date_pool as (
    select
          d.day::date as usage_date

        , case
            -- 주말/월요일은 이용량 많게
            when extract(dow from d.day) in (0, 1, 6) then
                4 + ((extract(day from d.day)::int + extract(month from d.day)::int) % 4)

            -- 평일은 2~5건
            else
                2 + ((extract(day from d.day)::int + extract(month from d.day)::int) % 4)
          end as daily_count

    from generate_series(
        date '2026-04-03',
        date '2026-06-04',
        interval '1 day'
    ) d(day)
),
slot_pool as (
    select
          dp.usage_date
        , gs.slot_no
        , dp.daily_count
    from date_pool dp
    join lateral generate_series(1, dp.daily_count) gs(slot_no)
      on true
),
completed_source as (
    select
          row_number() over(order by sp.usage_date, sp.slot_no) as n
        , sp.usage_date
        , sp.slot_no
        , sp.daily_count

        , vp.member_id
        , vp.vehicle_id
        , cp.charger_id
        , vp.battery_capacity_kwh
        , cp.charging_speed_kw
        , cp.price_per_kwh

        , (
            sp.usage_date
            + case
                when sp.slot_no % 7 = 1 then time '06:40'
                when sp.slot_no % 7 = 2 then time '08:30'
                when sp.slot_no % 7 = 3 then time '10:20'
                when sp.slot_no % 7 = 4 then time '12:40'
                when sp.slot_no % 7 = 5 then time '15:20'
                when sp.slot_no % 7 = 6 then time '18:10'
                else time '21:30'
              end
            + (((extract(day from sp.usage_date)::int + sp.slot_no) % 6) * interval '7 minutes')
        ) as start_time

        , 15 + (
            (
                extract(day from sp.usage_date)::int
                + sp.slot_no * 3
            ) % 45
          ) as current_soc

        , case
            when 15 + (
                (
                    extract(day from sp.usage_date)::int
                    + sp.slot_no * 3
                ) % 45
            ) >= 80 then 95
            else 80
          end as target_soc

    from slot_pool sp

    join vehicle_pool vp
      on vp.rn = (
          (
              extract(day from sp.usage_date)::int
              + sp.slot_no
              + extract(month from sp.usage_date)::int
          ) % vp.total_count
      ) + 1

    join charger_pool cp
      on cp.rn = (
          (
              extract(day from sp.usage_date)::int * 2
              + sp.slot_no
              + extract(month from sp.usage_date)::int
          ) % cp.total_count
      ) + 1
),
calculated as (
    select
          n
        , member_id
        , vehicle_id
        , charger_id
        , start_time

        , start_time + (
            ceil(
                (
                    battery_capacity_kwh
                    * ((target_soc - current_soc) / 100.0)
                    / nullif(charging_speed_kw, 0)
                    * 60
                    * 1.15
                )
            )::int * interval '1 minute'
          ) as end_time

        , current_soc
        , target_soc

        , round(
            (
                battery_capacity_kwh
                * ((target_soc - current_soc) / 100.0)
            )::numeric,
            2
          ) as required_kwh

        , ceil(
            (
                battery_capacity_kwh
                * ((target_soc - current_soc) / 100.0)
                / nullif(charging_speed_kw, 0)
                * 60
                * 1.15
            )
          )::int as estimated_minutes

        , round(
            (
                battery_capacity_kwh
                * ((target_soc - current_soc) / 100.0)
                * price_per_kwh
            )::numeric,
            0
          ) as estimated_cost

    from completed_source
),
inserted_reservation as (
    insert into reservation (
          member_id
        , vehicle_id
        , charger_id
        , reservation_date
        , start_time
        , end_time
        , current_soc
        , target_soc
        , required_kwh
        , estimated_minutes
        , estimated_cost
        , status
        , auth_code
        , verified_at
        , no_show_at
        , canceled_at
        , created_at
        , updated_at
    )
    select
          member_id
        , vehicle_id
        , charger_id
        , start_time::date
        , start_time
        , end_time
        , current_soc
        , target_soc
        , required_kwh
        , estimated_minutes
        , estimated_cost
        , '완료'
        , 'ADM_C_' || lpad(n::text, 5, '0')
        , start_time - interval '5 minutes'
        , null
        , null
        , start_time - interval '2 days'
        , end_time
    from calculated
    returning *
)
insert into charging_session (
      reservation_id
    , member_id
    , vehicle_id
    , charger_id
    , actual_start_time
    , actual_end_time
    , start_soc
    , end_soc
    , actual_kwh
    , actual_minutes
    , actual_cost
    , status
    , created_at
    , updated_at
)
select
      reservation_id
    , member_id
    , vehicle_id
    , charger_id
    , start_time
    , end_time
    , current_soc
    , target_soc
    , required_kwh
    , estimated_minutes
    , estimated_cost
    , '완료'
    , start_time
    , end_time
from inserted_reservation;


-- =========================================================
-- 현재/미래/취소/노쇼 예약 50건
-- =========================================================

with vehicle_pool as (
    select
          row_number() over(order by v.vehicle_id) as rn
        , count(*) over() as total_count
        , v.vehicle_id
        , v.member_id
        , vm.battery_capacity_kwh
    from vehicle v
    join vehicle_model vm
      on v.model_id = vm.model_id
    join app_member m
      on v.member_id = m.member_id
    where v.is_deleted = false
      and m.user_id like 'demo_user_%'
      and m.status = 'ACTIVE'
),
charger_pool as (
    select
          row_number() over(order by c.charger_id) as rn
        , count(*) over() as total_count
        , c.charger_id
        , c.charging_speed_kw
        , c.price_per_kwh
    from charger c
    join charging_station st
      on c.station_id = st.station_id
    where st.station_status = '운영중'
),
source as (
    select
          gs.n
        , vp.member_id
        , vp.vehicle_id
        , cp.charger_id
        , vp.battery_capacity_kwh
        , cp.charging_speed_kw
        , cp.price_per_kwh
        , case
            when gs.n between 1 and 8 then '충전중'
            when gs.n between 9 and 28 then '예약완료'
            when gs.n between 29 and 38 then '인증완료'
            when gs.n between 39 and 44 then '취소'
            else '노쇼'
          end as status
        , case
            when gs.n = 1 then current_timestamp - interval '50 minutes'
            when gs.n = 2 then current_timestamp - interval '45 minutes'
            when gs.n = 3 then current_timestamp - interval '40 minutes'
            when gs.n = 4 then current_timestamp - interval '35 minutes'
            when gs.n = 5 then current_timestamp - interval '30 minutes'
            when gs.n = 6 then current_timestamp - interval '25 minutes'
            when gs.n = 7 then current_timestamp - interval '20 minutes'
            when gs.n = 8 then current_timestamp - interval '15 minutes'
            when gs.n between 9 and 28 then current_timestamp + ((gs.n - 8) * interval '45 minutes')
            when gs.n between 29 and 38 then current_timestamp + ((gs.n - 28) * interval '30 minutes')
            when gs.n between 39 and 44 then current_timestamp - ((gs.n - 38) * interval '8 hours')
            else current_timestamp - ((gs.n - 44) * interval '10 hours')
          end as start_time
        , 20 + (gs.n % 40) as current_soc
        , 85 as target_soc
    from generate_series(1, 50) gs(n)
    join vehicle_pool vp
      on vp.rn = ((gs.n - 1) % vp.total_count) + 1
    join charger_pool cp
      on cp.rn = ((gs.n + 8 - 1) % cp.total_count) + 1
),
calculated as (
    select
          n
        , member_id
        , vehicle_id
        , charger_id
        , status
        , start_time
        , case
            when status = '충전중' then current_timestamp + ((n + 1) * interval '12 minutes')
            else start_time + interval '90 minutes'
          end as end_time
        , current_soc
        , target_soc
        , round(
            (
                battery_capacity_kwh
                * ((target_soc - current_soc) / 100.0)
            )::numeric,
            2
          ) as required_kwh
        , ceil(
            (
                battery_capacity_kwh
                * ((target_soc - current_soc) / 100.0)
                / nullif(charging_speed_kw, 0)
                * 60
                * 1.15
            )
          )::int as estimated_minutes
        , round(
            (
                battery_capacity_kwh
                * ((target_soc - current_soc) / 100.0)
                * price_per_kwh
            )::numeric,
            0
          ) as estimated_cost
    from source
),
inserted as (
    insert into reservation (
          member_id
        , vehicle_id
        , charger_id
        , reservation_date
        , start_time
        , end_time
        , current_soc
        , target_soc
        , required_kwh
        , estimated_minutes
        , estimated_cost
        , status
        , auth_code
        , verified_at
        , no_show_at
        , canceled_at
        , created_at
        , updated_at
    )
    select
          member_id
        , vehicle_id
        , charger_id
        , start_time::date
        , start_time
        , end_time
        , current_soc
        , target_soc
        , required_kwh
        , estimated_minutes
        , estimated_cost
        , status
        , 'ADM_R_' || lpad(n::text, 5, '0')
        , case when status in ('인증완료', '충전중') then start_time - interval '5 minutes' else null end
        , case when status = '노쇼' then start_time + interval '10 minutes' else null end
        , case when status = '취소' then start_time - interval '30 minutes' else null end
        , start_time - interval '1 day'
        , current_timestamp
    from calculated
    returning *
)
insert into charging_session (
      reservation_id
    , member_id
    , vehicle_id
    , charger_id
    , actual_start_time
    , actual_end_time
    , start_soc
    , end_soc
    , actual_kwh
    , actual_minutes
    , actual_cost
    , status
    , created_at
    , updated_at
)
select
      reservation_id
    , member_id
    , vehicle_id
    , charger_id
    , start_time
    , null
    , current_soc
    , null
    , null
    , null
    , null
    , '충전중'
    , start_time
    , null
from inserted
where status = '충전중';


-- 충전기 상태 동기화
update charger
set status = '사용가능',
    updated_at = current_timestamp
where status in ('사용가능', '예약중', '사용중');

update charger
set status = '사용중',
    updated_at = current_timestamp
where charger_id in (
    select distinct charger_id
    from reservation
    where auth_code like 'ADM%'
      and status = '충전중'
);

update charger
set status = '예약중',
    updated_at = current_timestamp
where charger_id in (
    select distinct charger_id
    from reservation
    where auth_code like 'ADM%'
      and status in ('예약완료', '인증완료')
)
and charger_id not in (
    select distinct charger_id
    from reservation
    where auth_code like 'ADM%'
      and status = '충전중'
);

update charger
set status = '점검중',
    updated_at = current_timestamp
where charger_id in (
    select charger_id
    from charger
    where charger_id not in (
        select charger_id
        from reservation
        where auth_code like 'ADM%'
          and status in ('예약완료', '인증완료', '충전중')
    )
    order by charger_id desc
    limit 2
);

update charger
set status = '고장',
    updated_at = current_timestamp
where charger_id in (
    select charger_id
    from charger
    where charger_id not in (
        select charger_id
        from reservation
        where auth_code like 'ADM%'
          and status in ('예약완료', '인증완료', '충전중')
    )
      and status <> '점검중'
    order by charger_id desc
    limit 2
);


-- 결과 확인
select status, count(*) from reservation where auth_code like 'ADM%' group by status order by status;

select status, count(*) from charging_session
where reservation_id in (
    select reservation_id from reservation where auth_code like 'ADM%'
)
group by status order by status;

select to_char(actual_start_time, 'YYYY-MM') as month,
       count(*) as session_count,
       sum(actual_kwh) as total_kwh,
       sum(actual_cost) as total_sales
from charging_session
where status = '완료'
  and reservation_id in (
      select reservation_id from reservation where auth_code like 'ADM%'
  )
group by to_char(actual_start_time, 'YYYY-MM')
order by month;

select status, count(*) from charger group by status order by status;

select
      cs.actual_start_time::date as usage_date
    , count(*) as usage_count
from charging_session cs
join reservation r
  on cs.reservation_id = r.reservation_id
where r.auth_code like 'ADM_C_%'
  and cs.status = '완료'
group by cs.actual_start_time::date
order by usage_date;

commit;