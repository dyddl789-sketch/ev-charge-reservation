-- 회원 테스트 전 초기화
truncate table
    ai_chat_message,
    ai_chat_room,
    charging_session,
    reservation,
    favorite_station,
    saved_location,
    vehicle,
    app_member
restart identity cascade;