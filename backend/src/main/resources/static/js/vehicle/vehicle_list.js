// 페이지 로딩 완료 후 이벤트 연결
$(function () {

    bindDefaultVehicleButtons();
	bindDeleteVehicleButtons();

});

// 기본 차량 설정 버튼 이벤트 연결
function bindDefaultVehicleButtons() {

    $(".default-btn").off("click").on("click", function () {

        const selectedVehicleId = $(this).data("vehicle-id");

        // 기본 차량 변경 요청
        $.ajax({
            url: contextPath + "/vehicle/default",
            type: "POST",
            data: {
                vehicleId: selectedVehicleId
            },

            success: function (result) {

                if (result !== "success") {
                    alert("기본 차량 설정에 실패했습니다.");
                    return;
                }

                // 화면 UI 즉시 변경
                updateDefaultVehicleUI(selectedVehicleId);
            },

            error: function () {
                alert("서버 오류가 발생했습니다.");
            }
        });

    });

}

// 기본 차량 UI 변경
function updateDefaultVehicleUI(selectedVehicleId) {

    // 전체 카드 초기화
    $(".vehicle-card").each(function () {

        const $card = $(this);

        // 기본 차량 스타일 제거
        $card.removeClass("main-vehicle");

        // 배지 제거
        $card.find(".badge").remove();

        // 상태 변경
        $card.find(".vehicle-status").text("대기");

        // 버튼 복원
        const vehicleId = $card.data("vehicle-id");

        $card.find(".outline-btn").replaceWith(`
            <button type="button"
                    class="outline-btn default-btn"
                    data-vehicle-id="${vehicleId}">
                기본 차량 설정
            </button>
        `);

    });

    // 선택 카드 찾기
    const $selectedCard = $(`
        .vehicle-card[data-vehicle-id="${selectedVehicleId}"]
    `);

    // 기본 차량 스타일 적용
    $selectedCard.addClass("main-vehicle");
	
	// 선택 차량 카드를 맨 위로 이동
	$(".vehicle-list").prepend($selectedCard);

    // 배지 추가
    $selectedCard
        .find(".vehicle-title-row > div")
        .prepend(`<span class="badge">기본 차량</span>`);

    // 상태 변경
    $selectedCard.find(".vehicle-status").text("사용중");

    // 버튼 변경
    $selectedCard.find(".outline-btn").replaceWith(`
        <button type="button"
                class="outline-btn active-btn"
                disabled>
            설정됨
        </button>
    `);

    // 상단 기본 차량 이름 변경
    const selectedName = $selectedCard.find("h2").text();

    $("#defaultVehicleName").text(selectedName);

    // 새 버튼 이벤트 다시 연결
    bindDefaultVehicleButtons();
}


// 차량 삭제 버튼 이벤트 연결
function bindDeleteVehicleButtons() {

    $(".delete-btn").off("click").on("click", function () {

        const vehicleId = $(this).data("vehicle-id");

        if (!confirm("해당 차량을 삭제하시겠습니까?")) {
            return;
        }

        $.ajax({
            url: contextPath + "/vehicle/delete",
            type: "POST",
            data: {
                vehicleId: vehicleId
            },
            success: function (result) {

                if (result !== "success") {
                    alert("차량 삭제에 실패했습니다.");
                    return;
                }

                $(`.vehicle-card[data-vehicle-id="${vehicleId}"]`).remove();

                alert("차량이 삭제되었습니다.");
            },
            error: function () {
                alert("서버 오류가 발생했습니다.");
            }
        });

    });
}
