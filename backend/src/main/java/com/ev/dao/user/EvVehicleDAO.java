package com.ev.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.dto.vehicle.EvVehicleModelDTO;

@Mapper
public interface EvVehicleDAO {

	// 차량 등록 화면 진입 시 차량 모델 목록 조회
    List<EvVehicleModelDTO> getVehicleModelList();
    
    // 내 차량 등록 
    void registerVehicle(EvVehicleDTO vehicleDTO);
    
    // 내 차량 목록 조회
    List<EvVehicleDTO> getVehicleList(Long memberId);
    
    // 기존 기본 차량 해제
    void clearDefaultVehicle(Long memberId);
    
    // 선택 차량 기본 차량 설정
    void updateDefaultVehicle(@Param("memberId") Long memberId,
                              @Param("vehicleId") Long vehicleId);
    // 차량삭제 (논리삭제 적용)
    void deleteVehicle(@Param("memberId") Long memberId,
            @Param("vehicleId") Long vehicleId);
}