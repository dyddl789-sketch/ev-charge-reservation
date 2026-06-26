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

        int safeLimit = limitPerRegion <= 0 ? 10 : Math.min(limitPerRegion, 20);
        int apiRowsPerRegion = Math.min(500, Math.max(120, safeLimit * 40));
        int maxChargersPerStation = 3;
        int totalSaveCount = 0;
        int failCount = 0;

        for (String region : SAMPLE_REGION_LIST) {
            try {
                log.info("@# 공공데이터 지역별 샘플 적재 region => {}", region);
                totalSaveCount += evPublicFastChargerSyncService.syncRegionStationSample(safeLimit, apiRowsPerRegion, region, maxChargersPerStation);
            } catch (Exception e) {
                failCount++;
                log.warn("@# 공공데이터 지역별 샘플 적재 실패 region => {}, message => {}", region, e.getMessage());
            }
        }

        int globalAugmentCount = evPublicFastChargerSyncService.augmentAllPublicApiSampleChargers(maxChargersPerStation);
        totalSaveCount += globalAugmentCount;
        log.info("@# 공공데이터 샘플 적재 후 전체 보강 충전기 생성 globalAugmentCount => {}", globalAugmentCount);

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
        resultDTO.setMessage("전국 시도별 샘플 충전소와 충전기를 공공데이터 기준으로 적재하고 부족 충전기를 보강했습니다.");
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

        /*
         * 발표 시작용 초기화.
         * 직접 등록한 관리자/회원/LOCAL 충전소는 유지하고,
         * 공공데이터 기반 충전소/충전기와 demo_user_%, stat_user_% 시연 회원만 정리한다.
         */
        int resetBrokenCount = evAdminOperationDAO.resetBrokenChargers();
        int notificationCount = evAdminOperationDAO.deleteDemoMemberNotifications();
        int attachmentCount = evAdminOperationDAO.deleteDemoAndPublicFileAttachments();
        int maintenanceCount = evAdminOperationDAO.deletePublicMaintenanceActions();
        int approvalCount = evAdminOperationDAO.deletePublicApprovalDocuments();
        int inspectionCount = evAdminOperationDAO.deletePublicInspections();
        int faultCount = evAdminOperationDAO.deletePublicFaultReports();
        int detachedComplaintCount = evAdminOperationDAO.detachPublicInfraFromComplaints();
        int publicStationCount = evAdminOperationDAO.deletePublicApiStations();
        int syncLogCount = evAdminOperationDAO.deletePublicApiSyncLogs();
        int demoMemberCount = evAdminOperationDAO.deleteDemoMembers();

        int totalResetCount = resetBrokenCount
                + notificationCount
                + attachmentCount
                + maintenanceCount
                + approvalCount
                + inspectionCount
                + faultCount
                + detachedComplaintCount
                + publicStationCount
                + syncLogCount
                + demoMemberCount;

        EvAdminOperationResultDTO resultDTO = new EvAdminOperationResultDTO();
        resultDTO.setSuccess(true);
        resultDTO.setMessage("발표용 초기화가 완료되었습니다. 직접 등록 데이터는 유지하고 공공데이터 충전소/충전기와 데모·통계 회원 데이터를 정리했습니다.");
        resultDTO.setFaultChargerList(evAdminOperationDAO.findRecentBrokenChargers());
        resultDTO.setResetCount(totalResetCount);
        resultDTO.setUpdateCount(detachedComplaintCount);
        resultDTO.setProcessedAt(LocalDateTime.now());

        log.info("@# 발표용 초기화 resetBrokenCount => {}, notificationCount => {}, attachmentCount => {}, maintenanceCount => {}, approvalCount => {}, inspectionCount => {}, faultCount => {}, detachedComplaintCount => {}, publicStationCount => {}, syncLogCount => {}, demoMemberCount => {}, totalResetCount => {}",
                resetBrokenCount,
                notificationCount,
                attachmentCount,
                maintenanceCount,
                approvalCount,
                inspectionCount,
                faultCount,
                detachedComplaintCount,
                publicStationCount,
                syncLogCount,
                demoMemberCount,
                totalResetCount);

        return resultDTO;
    }
    @Override
    public EvAdminOperationResultDTO generateStatisticsSampleData(int days, int count) {
        log.info("@# EvAdminOperationServiceImpl.generateStatisticsSampleData()");
        log.info("@# days => {}, count => {}", days, count);

        int safeDays = days <= 0 ? 120 : Math.min(days, 365);

        /*
         * 발표용 데이터는 화면이 풍부하게 보이는 수준이면 충분하다.
         * 15,000~40,000건을 한 번에 넣으면 DB 환경에 따라 5분 이상 걸릴 수 있어
         * 기본 6,000건, 최대 8,000건으로 제한하고 500건 단위로 나누어 생성한다.
         */
        int safeCount = count <= 0 ? 6000 : Math.min(count, 8000);
        int chunkSize = 500;

        int memberCount = evAdminOperationDAO.insertStatisticsDemoMembers();
        int vehicleCount = evAdminOperationDAO.insertStatisticsDemoVehicles();

        // 통계 샘플은 stat_user_% 회원 기준 데이터만 지우고 다시 생성한다.
        // 실제 회원, demo_user_% 회원, 관리자/직원 데이터는 건드리지 않는다.
        int deletedSessionCount = evAdminOperationDAO.deleteStatisticsSampleChargingSessions();
        int deletedReservationCount = evAdminOperationDAO.deleteStatisticsSampleReservations();

        int sessionCount = 0;
        int offset = 0;

        for (int created = 0; created < safeCount; created += chunkSize) {
            int currentChunkSize = Math.min(chunkSize, safeCount - created);

            log.info("@# statistics chunk start currentChunkSize => {}, offset => {}", currentChunkSize, offset);

            int chunkInsertCount = evAdminOperationDAO.insertStatisticsSampleSessions(
                    safeDays,
                    currentChunkSize,
                    offset
            );

            sessionCount += chunkInsertCount;
            offset += currentChunkSize * 2;

            log.info("@# statistics chunk end chunkInsertCount => {}, accumulatedSessionCount => {}",
                    chunkInsertCount, sessionCount);

            if (chunkInsertCount == 0 && created == 0) {
                log.warn("@# statistics sample first chunk inserted 0. stop generation.");
                break;
            }
        }

        int totalCompletedSessionCount = evAdminOperationDAO.countCompletedChargingSessions();

        EvAdminOperationResultDTO resultDTO = new EvAdminOperationResultDTO();
        resultDTO.setSuccess(sessionCount > 0);
        resultDTO.setMessage(sessionCount > 0
                ? "이용/매출 통계 샘플 데이터가 빠른 방식으로 재생성되었습니다."
                : "통계 샘플을 생성할 샘플 충전소 또는 충전기 데이터가 부족합니다. 먼저 공공데이터 샘플을 적재해 주세요.");
        resultDTO.setSaveCount(sessionCount);
        resultDTO.setUpdateCount(memberCount + vehicleCount);
        resultDTO.setResetCount(deletedSessionCount + deletedReservationCount);
        resultDTO.setProcessedAt(LocalDateTime.now());

        log.info("@# statistics memberCount => {}, vehicleCount => {}, deletedSessionCount => {}, deletedReservationCount => {}, sessionCount => {}, totalCompletedSessionCount => {}",
                memberCount, vehicleCount, deletedSessionCount, deletedReservationCount, sessionCount, totalCompletedSessionCount);

        return resultDTO;
    }

}
