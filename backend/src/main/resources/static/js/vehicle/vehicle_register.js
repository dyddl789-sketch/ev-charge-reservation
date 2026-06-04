// 차량 모델 선택 시 차량 정보 자동 변경
$("#modelId").on("change", function () {

    const selected =
        this.options[this.selectedIndex];

    $("#manufacturer").val(
        selected.dataset.manufacturer || ""
    );

    $("#batteryCapacity").val(
        selected.dataset.battery
            ? selected.dataset.battery + " kWh"
            : ""
    );

    $("#connectorType").val(
        selected.dataset.connector || ""
    );

    $("#maxChargingSpeed").val(
        selected.dataset.speed
            ? selected.dataset.speed + " kW"
            : ""
    );

});