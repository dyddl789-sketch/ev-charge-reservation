package com.ev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/station")
public class StationController {

    // 충전소 찾기 / 목록 화면
    @GetMapping("/list")
    public String stationList() {
        return "station/station_list";
    }

    // 충전소 상세 화면은 다음 단계에서 만들 예정
    @GetMapping("/detail")
    public String stationDetail() {
        return "station/station_detail";
    }
}