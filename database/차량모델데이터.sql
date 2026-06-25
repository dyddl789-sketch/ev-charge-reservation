insert into vehicle_model (
    manufacturer,
    model_name,
    battery_capacity_kwh,
    connector_type,
    max_charging_speed_kw
)
values
('현대', '아이오닉5', 77.4, 'DC콤보', 350),
('기아', 'EV6', 77.4, 'DC콤보', 350),
('테슬라', '모델3', 60.0, 'NACS', 250),
('BMW', 'i4', 83.9, 'DC콤보', 200),
('벤츠', 'EQS', 107.8, 'DC콤보', 200);



update vehicle_model
set image_url = '/images/vehicle/아이오닉5.jpg'
where manufacturer = '현대'
and model_name = '아이오닉5';

update vehicle_model
set image_url = '/images/vehicle/EV6.jpg'
where manufacturer = '기아'
and model_name = 'EV6';

update vehicle_model
set image_url = '/images/vehicle/모델3.jpg'
where manufacturer = '테슬라'
and model_name = '모델3';

update vehicle_model
set image_url = '/images/vehicle/i4.png'
where manufacturer = 'BMW'
and model_name = 'i4';

update vehicle_model
set image_url = '/images/vehicle/eq5.png'
where manufacturer = '벤츠'
and model_name = 'EQS';

select * from vehicle_model;
