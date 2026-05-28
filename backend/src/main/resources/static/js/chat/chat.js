// HTML 로드 완료 후 실행
$(function () {

    // ================================
    // 요소 가져오기
    // ================================

    const $toggleBtn = $('#evChatToggleBtn');
    const $closeBtn = $('#evChatCloseBtn');
    const $minBtn = $('#evChatMinBtn');
    const $expandBtn = $('#evChatExpandBtn');

    const $chatBox = $('#evChatBox');
    const $input = $('#evChatInput');
    const $sendBtn = $('#evChatSendBtn');
    const $messages = $('#evChatMessages');

    // 이전 대화 로드 여부
    let isHistoryLoaded = false;

    // 요소 없으면 종료
    if (
        $toggleBtn.length === 0 ||
        $chatBox.length === 0 ||
        $input.length === 0 ||
        $sendBtn.length === 0 ||
        $messages.length === 0
    ) {
        return;
    }

    // ================================
    // 채팅창 열기/닫기
    // ================================

    $toggleBtn.on('click', function () {

        $chatBox.toggleClass('active');

        if ($chatBox.hasClass('active')) {

            $input.focus();

            if (!isHistoryLoaded) {
                loadChatHistory();
            }
        }
    });

    // 최소화 버튼
    $minBtn.on('click', function () {
        $chatBox.removeClass('active');
    });

    // 닫기 버튼
    $closeBtn.on('click', function () {
        $chatBox.removeClass('active');
    });

	// 확대/축소 버튼
	$expandBtn.on('click', function () {

	    if ($chatBox.hasClass('expanded')) {

	        // 축소
	        $chatBox.removeClass('expanded');
	        $chatBox.removeClass('wide');

	        $chatBox.css({
	            width: '',
	            height: ''
	        });

	    } else {

	        // 확대 전 수동 크기 제거
	        $chatBox.css({
	            width: '',
	            height: ''
	        });

	        // 확대
	        $chatBox.addClass('expanded');
	        $chatBox.addClass('wide');
	    }

	    $messages.scrollTop(
	        $messages[0].scrollHeight
	    );
	});

    // ================================
    // 대표 기능 버튼
    // ================================

    $(document).on('click', '.ev-chat-quick-btn', function () {

        const message = $(this).data('message');

        $input.val(message);

        sendMessage();
    });

    // ================================
    // 메시지 전송 이벤트
    // ================================

    $sendBtn.on('click', function () {
        sendMessage();
    });

    $input.on('keydown', function (event) {

        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    // ================================
    // 이전 채팅 조회
    // ================================

    function loadChatHistory() {

        isHistoryLoaded = true;

        $.ajax({

            url: EV_CONTEXT_PATH + '/ai-chat/history',
            type: 'GET',
            dataType: 'json',

			success: function (data) {

			    console.log('chat history =>', data);

			    // 기존 메시지 제거
			    $messages.empty();

			    // 이전 대화가 없으면 기본 안내 메시지 출력
			    if (!data || data.length === 0) {

			        appendMessage(
			            'ai',
			            '안녕하세요! 전기차 충전 예약을 도와드리는 AI 챗봇입니다.\n궁금한 기능을 선택하거나 직접 입력해 주세요.'
			        );

			        appendQuickActions();

			        return;
			    }

			    // 이전 메시지 출력
			    $.each(data, function (index, item) {

			        const sender =
			            item.senderType === 'USER' ? 'user' : 'ai';

			        appendMessage(sender, item.message);
			    });

			    // 이전 대화가 있어도 대표 기능 버튼 출력
			    appendQuickActions();
			},

            error: function (xhr, textStatus) {

                console.log('history status =>', xhr.status);
                console.log('history textStatus =>', textStatus);

                isHistoryLoaded = false;
            }
        });
    }

    // ================================
    // 메시지 전송
    // ================================

    function sendMessage() {

        const message = $.trim($input.val());

        if (message === '') {
            return;
        }

        // 대표 기능 버튼 제거
        $('#evChatQuickActions').remove();

        // 사용자 메시지 출력
        appendMessage('user', message);

        // 입력창 초기화
        $input.val('');

        // 중복 전송 방지
        $sendBtn.prop('disabled', true);

        // 로딩 메시지
        const $loadingElement =
            appendMessage('ai', '답변을 생성하고 있습니다...', true);

        $.ajax({

            url: EV_CONTEXT_PATH + '/ai-chat/send',
            type: 'POST',
            contentType: 'application/json; charset=UTF-8',
            dataType: 'json',

            data: JSON.stringify({
                message: message
            }),

            success: function (data) {

                $loadingElement.remove();

                if (data && data.answer) {
                    appendMessage('ai', data.answer);
                } else {
                    appendMessage('ai', '응답을 가져오지 못했습니다.');
                }
            },

            error: function (xhr, textStatus, errorThrown) {

                console.log('status =>', xhr.status);
                console.log('textStatus =>', textStatus);
                console.log('errorThrown =>', errorThrown);
                console.log('responseText =>', xhr.responseText);

                $loadingElement.remove();

                if (
                    xhr.status === 401 ||
                    xhr.status === 403 ||
                    xhr.status === 302 ||
                    textStatus === 'parsererror' ||
                    xhr.responseText.includes('<!DOCTYPE html>')
                ) {
                    appendMessage(
                        'ai',
                        'AI 챗봇은 로그인 후 이용할 수 있습니다.'
                    );
                    return;
                }

                appendMessage(
                    'ai',
                    '일시적으로 챗봇 응답을 불러오지 못했습니다.'
                );
            },

            complete: function () {

                $sendBtn.prop('disabled', false);

                $input.focus();
            }
        });
    }

	// 대표 기능 버튼 출력
	function appendQuickActions() {

	    $('#evChatQuickActions').remove();

	    const $quickActions = $('<div></div>')
	        .attr('id', 'evChatQuickActions')
	        .addClass('ev-chat-quick-actions');

	    const actions = [
	        {
	            className: 'station-btn',
	            title: '내 차량에 맞는 근처 충전소 찾기',
	            message: '내 차량에 맞는 근처 충전소 찾아줘'
	        },
	        {
	            className: 'reservation-btn',
	            title: '충전 예약 방법',
	            message: '충전 예약 방법 알려줘'
	        },
	        {
	            className: 'time-btn',
	            title: '내 차 충전 시간 계산',
	            message: '내 차 충전 시간 계산해줘'
	        },
	        {
	            className: 'cost-btn',
	            title: '충전 비용 계산',
	            message: '충전 비용이 얼마나 나올까?'
	        }
	    ];

	    $.each(actions, function (index, action) {

	        const $button = $('<button></button>')
	            .attr('type', 'button')
	            .addClass('ev-chat-quick-btn')
	            .addClass(action.className)
	            .attr('data-message', action.message)
	            .text(action.title);

	        $quickActions.append($button);
	    });

	    $messages.append($quickActions);

	    $messages.scrollTop(
	        $messages[0].scrollHeight
	    );
	}
	
    // ================================
    // 메시지 화면 추가
    // ================================

	// 채팅 메시지 추가
	function appendMessage(sender, text, isLoading) {

	    // 메시지 영역 생성
	    const $messageElement = $('<div></div>')
	        .addClass('ev-chat-message')
	        .addClass(sender);

	    // 로딩 메시지 여부
	    if (isLoading) {
	        $messageElement.addClass('ev-chat-loading');
	    }

	    // AI 메시지일 때 아바타 추가
	    if (sender === 'ai') {

	        const $avatarElement = $('<div></div>')
	            .addClass('ev-chat-avatar');

	        const $avatarImage = $('<img>')
	            .attr(
	                'src',
	                EV_CONTEXT_PATH + '/images/chat/ai-avatar.png'
	            )
	            .attr('alt', 'EV Charge AI');

	        $avatarElement.append($avatarImage);

	        $messageElement.append($avatarElement);
	    }

	    // 말풍선 생성
	    const $bubbleElement = $('<div></div>')
	        .addClass('ev-chat-bubble')
	        .text(text);

	    // 메시지 추가
	    $messageElement.append($bubbleElement);

	    // 채팅창에 추가
	    $messages.append($messageElement);

	    // 스크롤 아래 유지
	    $messages.scrollTop(
	        $messages[0].scrollHeight
	    );

	    return $messageElement;
	}

    // ================================
    // 왼쪽 상단 리사이즈
    // ================================

    let isResizing = false;
    let resizeStartX = 0;
    let resizeStartY = 0;
    let startWidth = 0;
    let startHeight = 0;

    $('.ev-chat-resize-handle.top-left').on('mousedown', function (event) {

        event.preventDefault();
        event.stopPropagation();

        isResizing = true;

        resizeStartX = event.clientX;
        resizeStartY = event.clientY;

        const rect = $chatBox[0].getBoundingClientRect();

        startWidth = rect.width;
        startHeight = rect.height;

        $('body').css('user-select', 'none');
    });
	
	// 채팅창 크기 조절 이벤트
	$(document).on('mousemove', function (event) {

	    if (!isResizing) {
	        return;
	    }

	    const dx = event.clientX - resizeStartX;
	    const dy = event.clientY - resizeStartY;

	    let newWidth = startWidth - dx;
	    let newHeight = startHeight - dy;

	    const minWidth = 340;
	    const minHeight = 440;

	    const maxWidth = 720;
	    const maxHeight = window.innerHeight * 0.82;

	    newWidth = Math.max(
	        minWidth,
	        Math.min(newWidth, maxWidth)
	    );

	    newHeight = Math.max(
	        minHeight,
	        Math.min(newHeight, maxHeight)
	    );

	    // 직접 조절 시 확대 상태 해제
	    $chatBox.removeClass('expanded');

	    // 크기 적용
	    $chatBox.css({
	        width: newWidth + 'px',
	        height: newHeight + 'px'
	    });

	    // 채팅창 너비에 따라 대표 기능 버튼 열 수 변경
	    if (newWidth >= 560) {

	        $chatBox.addClass('wide');

	    } else {

	        $chatBox.removeClass('wide');
	    }

	    // 스크롤 아래 유지
	    $messages.scrollTop(
	        $messages[0].scrollHeight
	    );
	});
    $(document).on('mouseup', function () {

        if (!isResizing) {
            return;
        }

        isResizing = false;

        $('body').css('user-select', '');
    });
});