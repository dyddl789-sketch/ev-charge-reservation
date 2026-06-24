package com.ev.service.admin;

import com.ev.dto.admin.simulation.EvAdminOperationResultDTO;

/*
 * MIS 운영 시뮬레이션 Service
 */
public interface EvAdminOperationService {

    EvAdminOperationResultDTO syncPublicDataSample(int limitPerRegion);

    EvAdminOperationResultDTO triggerFaultSimulation();

    EvAdminOperationResultDTO resetSimulation();
}
