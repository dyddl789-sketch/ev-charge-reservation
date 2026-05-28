<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat/chat.css">

<script src="${pageContext.request.contextPath}/js/jquery.js"></script>

<div class="ev-chat-widget" id="evChatWidget">

    <!-- AI 상담 버튼 -->
	<button type="button" class="ev-chat-toggle-btn" id="evChatToggleBtn" aria-label="AI 상담 열기">
	    <span class="ev-chat-icon">
	        <span class="ev-chat-icon-dot"></span>
	        <span class="ev-chat-icon-dot"></span>
	        <span class="ev-chat-icon-dot"></span>
	    </span>
	</button>

    <!-- AI 채팅창 -->
    <div class="ev-chat-box" id="evChatBox">

        <!-- 채팅창 크기 조절 핸들 -->
        <div class="ev-chat-resize-handle top-left"></div>


        <!-- 채팅창 헤더 -->
        <div class="ev-chat-header">
            <div>
                <div class="ev-chat-title">EV Charge AI 챗봇</div>
                <div class="ev-chat-subtitle">충전소 · 예약 · 차량 문의 도우미</div>
            </div>

			<div class="ev-chat-header-actions">
			    <button type="button" class="ev-chat-header-btn" id="evChatMinBtn">−</button>
			    <button type="button" class="ev-chat-header-btn" id="evChatExpandBtn">□</button>
			    <button type="button" class="ev-chat-header-btn" id="evChatCloseBtn">×</button>
			</div>
        </div>

        <!-- 채팅 메시지 영역 -->
        <div class="ev-chat-messages" id="evChatMessages">

			<div class="ev-chat-message ai">
			    <!-- AI 아바타 -->
			    <div class="ev-chat-avatar">
			        <img src="${pageContext.request.contextPath}/images/chat/ai-avatar.png"
			             alt="EV Charge AI">
			    </div>
			
			    <div class="ev-chat-bubble">
			        안녕하세요! 전기차 충전 예약을 도와드리는 AI 챗봇입니다.
			        궁금한 기능을 선택하거나 직접 입력해 주세요.
			    </div>
			</div>

            <!-- 대표 기능 버튼 -->
            <div class="ev-chat-quick-actions" id="evChatQuickActions">

				<button type="button"
				        class="ev-chat-quick-btn station-btn"
				        data-message="내 차량에 맞는 근처 충전소 찾아줘">
				    내 차량에 맞는 근처 충전소 찾기
				</button>
				
				<button type="button"
				        class="ev-chat-quick-btn reservation-btn"
				        data-message="충전 예약 방법 알려줘">
				    충전 예약 방법
				</button>
				
				<button type="button"
				        class="ev-chat-quick-btn time-btn"
				        data-message="내 차 충전 시간 계산해줘">
				    내 차 충전 시간 계산
				</button>
				
				<button type="button"
				        class="ev-chat-quick-btn cost-btn"
				        data-message="충전 비용이 얼마나 나올까?">
				    충전 비용 계산
				</button>

            </div>
        </div>

        <!-- 채팅 입력 영역 -->
        <div class="ev-chat-input-area">
            <input type="text"
                   id="evChatInput"
                   class="ev-chat-input"
                   placeholder="메시지를 입력하세요."
                   autocomplete="off">

            <button type="button"
                    id="evChatSendBtn"
                    class="ev-chat-send-btn">
                전송
            </button>
        </div>
    </div>
</div>

<script>
    const EV_CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>

<script src="${pageContext.request.contextPath}/js/chat/chat.js"></script>