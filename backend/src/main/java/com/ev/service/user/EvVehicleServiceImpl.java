package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvVehicleDAO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.dto.vehicle.EvVehicleModelDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvVehicleServiceImpl implements EvVehicleService {

    private final EvVehicleDAO evVehicleDAO;
 // 차량 등록 화면 진입 시 차량 모델 목록 조회
    @Override
    public List<EvVehicleModelDTO> getVehicleModelList() {
        return evVehicleDAO.getVehicleModelList();
    }
//    내 차량 등록
    @Override
    @Transactional
    public void registerVehicle(EvVehicleDTO vehicleDTO) {
    	evVehicleDAO.registerVehicle(vehicleDTO);
    }
//    내 차량 목록 조회 
    @Override
    public List<EvVehicleDTO> getVehicleList(Long memberId) {
        return evVehicleDAO.getVehicleList(memberId);
    }
    
//    기본 차량 설정 ajax방식
    @Override
    @Transactional
    public void setDefaultVehicle(Long memberId, Long vehicleId) {

        // 기존 기본 차량 해제
    	evVehicleDAO.clearDefaultVehicle(memberId);

        // 선택 차량 기본 차량 설정
    	evVehicleDAO.updateDefaultVehicle(memberId, vehicleId);

    }
    
//    차량 삭제 논리삭제 적용
    @Override
    @Transactional
    public void deleteVehicle(Long memberId, Long vehicleId) {
        evVehicleDAO.deleteVehicle(memberId, vehicleId);
    }
}
