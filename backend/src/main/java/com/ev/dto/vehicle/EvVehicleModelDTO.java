package com.ev.dto.vehicle;

import lombok.Data;

@Data
public class EvVehicleModelDTO {

    private Long modelId;

    private String manufacturer;

    private String modelName;

    private Double batteryCapacityKwh;

    private String connectorType;

    private Double maxChargingSpeedKw;
    
    private String imageUrl;
}