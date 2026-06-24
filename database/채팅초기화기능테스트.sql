/* =========================================================
   AI 대화 초기화 적용 확인 SQL
   목적: DELETE /ai-chat/messages 호출 전후로 현재 로그인 사용자의
        AI 채팅 메시지가 삭제되는지 확인한다.

   사용 방법
   1) 아래 user_id 값을 테스트 계정 아이디로 변경
   2) 초기화 전 메시지 수 확인
   3) 화면에서 AI 대화 초기화 버튼 클릭
   4) 다시 실행해서 message_count가 0인지 확인
========================================================= */

select
      m.member_id
    , m.user_id
    , r.room_id
    , count(msg.message_id) as message_count
from app_member m
left join ai_chat_room r
       on r.member_id = m.member_id
left join ai_chat_message msg
       on msg.room_id = r.room_id
where m.user_id = 'test'
group by m.member_id, m.user_id, r.room_id
order by r.room_id desc;

/* Redis는 DBeaver가 아니라 Redis Desktop Manager 또는 redis-cli에서 확인한다.
   삭제 대상 key 예시
   - ai:chat:recent:{roomId}
   - ai:chat:room:{roomId}
   - ai:reservation:candidate:{memberId}
*/
