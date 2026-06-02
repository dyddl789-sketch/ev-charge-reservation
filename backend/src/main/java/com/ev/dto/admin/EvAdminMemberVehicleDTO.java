package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class EvAdminMemberVehicleDTO {

    private Long vehicleId;
    private String vehicleNickname;
    private String plateNumber;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    private String manufacturer;
    private String modelName;

    public String getCreatedAtText() {
        if (createdAt == null) return "-";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}