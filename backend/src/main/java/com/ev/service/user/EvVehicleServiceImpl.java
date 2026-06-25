package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvVehicleDAO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.dto.vehicle.EvVehicleModelDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvVehicleServiceImpl implements EvVehicleService {
	
	private final EvAiChatService evAiChatService;
    private final EvVehicleDAO evVehicleDAO;
 // 차량 등록 화면 진입 시 차량 모델 목록 조회
    @Override
    public List<EvVehicleModelDTO> getVehicleModelList() {
        return evVehicleDAO.getVehicleModelList();
    }
    //내 차량등록
    @Override
    @Transactional
    public void registerVehicle(EvVehicleDTO vehicleDTO) {
        log.info("@# EvVehicleServiceImpl.registerVehicle()");
        log.info("@# memberId => {}", vehicleDTO == null ? null : vehicleDTO.getMemberId());

        if (vehicleDTO == null) {
            throw new IllegalArgumentException("차량 등록 정보가 없습니다.");
        }

        if (vehicleDTO.getModelId() == null) {
            throw new IllegalArgumentException("차량 모델을 선택해 주세요.");
        }

        if (vehicleDTO.getPlateNumber() == null
                || vehicleDTO.getPlateNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("차량번호를 입력해 주세요.");
        }

        String plateNumber = vehicleDTO.getPlateNumber()
                .replaceAll("\\s+", "")
                .trim();

        vehicleDTO.setPlateNumber(plateNumber);

        int count = evVehicleDAO.countByPlateNumber(
                vehicleDTO.getMemberId(),
                plateNumber
        );

        if (count > 0) {
            throw new IllegalArgumentException("이미 등록된 차량번호입니다.");
        }

        // 기본차량으로 등록하는 경우 같은 회원의 기존 기본차량을 먼저 해제한다.
        // 프론트에서 체크박스를 제어해도 최종 보장은 백엔드 트랜잭션에서 처리한다.
        if (Boolean.TRUE.equals(vehicleDTO.getIsDefault())) {
            log.info("@# 신규 차량을 기본차량으로 등록 - 기존 기본차량 해제");
            evVehicleDAO.clearDefaultVehicle(vehicleDTO.getMemberId());
        }

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
    	
        // AI 대화 캐시 삭제
        evAiChatService.clearChatCache(memberId);

        log.info("@# 대표 차량 변경으로 AI 캐시 삭제 완료");
    	

    }
    
//    차량 삭제 논리삭제 적용
    @Override
    @Transactional
    public void deleteVehicle(Long memberId, Long vehicleId) {
        evVehicleDAO.deleteVehicle(memberId, vehicleId);
    }
    
    
}
