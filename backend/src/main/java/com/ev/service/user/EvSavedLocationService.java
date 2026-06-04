package com.ev.service.user;

import java.util.List;

import com.ev.dto.map.EvSavedLocationDTO;

/*
 * 저장 위치 Service
 *
 * Controller와 DAO 사이에서 저장 위치 관련 로직을 담당한다.
 */
public interface EvSavedLocationService {

    // 로그인 회원의 저장 위치 목록 조회
    List<EvSavedLocationDTO> findSavedLocationList(Long memberId);

    // 저장 위치 등록
    void saveSavedLocation(EvSavedLocationDTO savedLocationDTO);

    // 기본 저장 위치 설정
    void setDefaultLocation(Long memberId, Long locationId);

    // 저장 위치 삭제
    void deleteSavedLocation(Long memberId, Long locationId);
}
