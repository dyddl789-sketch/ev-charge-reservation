package com.ev.dto.vehicle;

import lombok.Data;

@Data
public class EvVehicleDTO {

    private Long vehicleId;
    private Long memberId;
    private Long modelId;

    private String vehicleNickname;
    private String plateNumber;
    private Boolean isDefault;
    
    // vehicle_model join 데이터
    private String manufacturer;
    private String modelName;
    private Double batteryCapacityKwh;
    private String connectorType;
    private Double maxChargingSpeedKw;
    private String imageUrl;
    
}
