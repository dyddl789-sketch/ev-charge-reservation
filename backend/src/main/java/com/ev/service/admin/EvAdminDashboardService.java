package com.ev.service.admin;

import java.time.LocalDate;

import com.ev.dto.admin.EvAdminDashboardDTO;

public interface EvAdminDashboardService {

    EvAdminDashboardDTO getDashboard(LocalDate date);
}