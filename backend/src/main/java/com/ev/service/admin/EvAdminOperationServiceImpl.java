package com.ev.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.admin.EvAdminOperationDAO;
import com.ev.dto.admin.simulation.EvAdminOperationResultDTO;
import com.ev.dto.admin.simulation.EvAdminSimulationChargerDTO;
import com.ev.service.user.EvPublicFastChargerSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * MIS 대시보드 운영 버튼 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminOperationServiceImpl implements EvAdminOperationService {

    private final EvAdminOperationDAO evAdminOperationDAO;
    private final EvPublicFastChargerSyncService evPublicFastChargerSyncService;

    private static final List<String> SAMPLE_REGION_LIST = List.of(
            "서울특별시",
            "부산광역시",
            "대구광역시",
            "인천광역시",
            "광주광역시",
            "대전광역시",
            "울산광역시",
            "세종특별자치시",
            "경기도",
            "강원특별자치도",
            "충청북도",
            "충청남도",
            "전북특별자치도",
            "전라남도",
            "경상북도",
            "경상남도",
            "제주특별자치도"
    );

    @Override
    @Transactional
    public EvAdminOperationResultDTO syncPublicDataSample(int limitPerRegion) {
        log.info("@# EvAdminOperationServiceImpl.syncPublicDataSample()");
        log.info("@# limitPerRegion => {}", limitPerRegion);

        int safeLimit = limitPerRegion <= 0 ? 10 : Math.min(limitPerRegion, 30);
        int totalSaveCount = 0;
        int failCount = 0;

        for (String region : SAMPLE_REGION_LIST) {
            try {
                log.info("@# 공공데이터 지역별 샘플 적재 region => {}", region);
                totalSaveCount += evPublicFastChargerSyncService.syncPage(1, safeLimit, region);
            } catch (Exception e) {
                failCount++;
                log.warn("@# 공공데이터 지역별 샘플 적재 실패 region => {}, message => {}", region, e.getMessage());
            }
        }

        String status = failCount == 0 ? "성공" : (totalSaveCount > 0 ? "부분성공" : "실패");

        evAdminOperationDAO.insertPublicApiSyncLog(
                "환경부 공공급속 충전기 API",
                "REGION_SAMPLE",
                status,
                SAMPLE_REGION_LIST.size(),
                totalSaveCount,
                failCount,
                failCount == 0 ? null : "일부 지역 적재 실패"
        );

        EvAdminOperationResultDTO resultDTO = new EvAdminOperationResultDTO();
        resultDTO.setSuccess(totalSaveCount > 0);
        resultDTO.setMessage("전국 시도별 공공데이터 샘플 적재가 완료되었습니다.");
        resultDTO.setSaveCount(totalSaveCount);
        resultDTO.setLimitPerRegion(safeLimit);
        resultDTO.setRegionList(SAMPLE_REGION_LIST);
        resultDTO.setProcessedAt(LocalDateTime.now());

        log.info("@# 공공데이터 샘플 적재 totalSaveCount => {}", totalSaveCount);

        return resultDTO;
    }

    @Override
    @Transactional
    public EvAdminOperationResultDTO triggerFaultSimulation() {
        log.info("@# EvAdminOperationServiceImpl.triggerFaultSimulation()");

        EvAdminSimulationChargerDTO target = evAdminOperationDAO.findRandomAvailableBusanCharger();

        EvAdminOperationResultDTO resultDTO = new EvAdminOperationResultDTO();
        resultDTO.setProcessedAt(LocalDateTime.now());

        if (target == null) {
            log.warn("@# 장애 시뮬레이션 대상 사용가능 충전기 없음");
            resultDTO.setSuccess(false);
            resultDTO.setMessage("장애 시뮬레이션 대상이 없습니다. 사용가능 충전기가 있는지 확인해 주세요.");
            return resultDTO;
        }

        int updateCount = evAdminOperationDAO.updateChargerStatus(target.getChargerId(), "고장");

        int openFaultCount = evAdminOperationDAO.countOpenFaultByCharger(target.getChargerId());
        if (openFaultCount == 0) {
            evAdminOperationDAO.insertSystemFaultFromCharger(
                    target.getChargerId(),
                    "통신장애",
                    target.getStationName() + " " + target.getChargerName() + " 장애 감지",
                    "시스템 시뮬레이션으로 충전기 이상 상태가 감지되었습니다.",
                    "HIGH",
                    "시스템감지"
            );
        }

        resultDTO.setSuccess(updateCount > 0);
        resultDTO.setMessage("부산 지역 장애 시뮬레이션이 발생했습니다. 충전기 1대가 고장 상태로 변경되고 장애점검관리에 등록되었습니다.");
        resultDTO.setUpdateCount(updateCount);
        resultDTO.setChargerId(target.getChargerId());
        resultDTO.setStationId(target.getStationId());
        resultDTO.setStationName(target.getStationName());
        resultDTO.setChargerName(target.getChargerName());
        resultDTO.setBeforeStatus(target.getStatus());
        resultDTO.setAfterStatus("고장");
        resultDTO.setFaultChargerList(evAdminOperationDAO.findRecentBrokenChargers());

        log.info("@# 장애 시뮬레이션 대상 chargerId => {}, stationName => {}, chargerName => {}",
                target.getChargerId(), target.getStationName(), target.getChargerName());

        return resultDTO;
    }

    @Override
    @Transactional
    public EvAdminOperationResultDTO resetSimulation() {
        log.info("@# EvAdminOperationServiceImpl.resetSimulation()");

        int resetCount = evAdminOperationDAO.resetBrokenChargers();

        EvAdminOperationResultDTO resultDTO = new EvAdminOperationResultDTO();
        resultDTO.setSuccess(true);
        resultDTO.setMessage("시뮬레이션 초기화가 완료되었습니다. 고장 상태 충전기를 사용가능 상태로 되돌렸습니다.");
        resultDTO.setFaultChargerList(evAdminOperationDAO.findRecentBrokenChargers());
        resultDTO.setResetCount(resetCount);
        resultDTO.setProcessedAt(LocalDateTime.now());

        log.info("@# 시뮬레이션 초기화 resetCount => {}", resetCount);

        return resultDTO;
    }
    @Override
    @Transactional
    public EvAdminOperationResultDTO generateStatisticsSampleData(int days, int count) {
        log.info("@# EvAdminOperationServiceImpl.generateStatisticsSampleData()");
        log.info("@# days => {}, count => {}", days, count);

        int safeDays = days <= 0 ? 60 : Math.min(days, 365);
        int safeCount = count <= 0 ? 300 : Math.min(count, 5000);

        int memberCount = evAdminOperationDAO.insertStatisticsDemoMembers();
        int vehicleCount = evAdminOperationDAO.insertStatisticsDemoVehicles();
        int sessionCount = evAdminOperationDAO.insertStatisticsSampleSessions(safeDays, safeCount);
        int totalCompletedSessionCount = evAdminOperationDAO.countCompletedChargingSessions();

        EvAdminOperationResultDTO resultDTO = new EvAdminOperationResultDTO();
        resultDTO.setSuccess(sessionCount > 0);
        resultDTO.setMessage(sessionCount > 0
                ? "이용/매출 통계 샘플 데이터가 생성되었습니다."
                : "통계 샘플을 생성할 충전기 또는 차량 데이터가 부족합니다. 먼저 공공데이터 샘플을 적재해 주세요.");
        resultDTO.setSaveCount(sessionCount);
        resultDTO.setUpdateCount(memberCount + vehicleCount);
        resultDTO.setResetCount(totalCompletedSessionCount);
        resultDTO.setProcessedAt(LocalDateTime.now());

        log.info("@# statistics memberCount => {}, vehicleCount => {}, sessionCount => {}",
                memberCount, vehicleCount, sessionCount);

        return resultDTO;
    }

}
