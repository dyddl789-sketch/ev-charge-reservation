package com.ev.service.user;

import java.util.List;

import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.dto.vehicle.EvVehicleModelDTO;

public interface EvVehicleService {

	 // 차량 등록 화면 진입 시 차량 모델 목록 조회
    List<EvVehicleModelDTO> getVehicleModelList();
    // 내 차량등록
    void registerVehicle(EvVehicleDTO vehicleDTO);
    // 내 차량 목록 조회 
    List<EvVehicleDTO> getVehicleList(Long memberId);
    //기본 차량 설정 ajax방식
    void setDefaultVehicle(Long memberId, Long vehicleId);
    //차량 삭제 논리삭제 적용
    void deleteVehicle(Long memberId, Long vehicleId);
}
