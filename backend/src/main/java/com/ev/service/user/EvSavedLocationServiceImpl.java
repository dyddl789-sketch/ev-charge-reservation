package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvSavedLocationDAO;
import com.ev.dto.map.EvSavedLocationDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 저장 위치 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvSavedLocationServiceImpl implements EvSavedLocationService {

    private final EvSavedLocationDAO savedLocationDAO;

    /*
     * 로그인 회원의 저장 위치 목록 조회
     */
    @Override
    public List<EvSavedLocationDTO> findSavedLocationList(Long memberId) {
        log.info("@# EvSavedLocationServiceImpl.findSavedLocationList()");
        log.info("@# memberId => {}", memberId);

        return savedLocationDAO.findSavedLocationList(memberId);
    }

    /*
     * 저장 위치 등록
     *
     * 기본 위치로 저장하는 경우,
     * 기존 기본 위치를 먼저 해제하고 새 위치를 등록한다.
     *
     * 단순 insert만 하면 기본 위치가 여러 개 생길 수 있으므로
     * isDefault=true일 때 clearDefaultLocation()을 먼저 실행한다.
     */
    @Override
    @Transactional
    public void saveSavedLocation(EvSavedLocationDTO savedLocationDTO) {
        log.info("@# EvSavedLocationServiceImpl.saveSavedLocation()");
        log.info("@# savedLocationDTO => {}", savedLocationDTO);

        if (Boolean.TRUE.equals(savedLocationDTO.getIsDefault())) {
            savedLocationDAO.clearDefaultLocation(savedLocationDTO.getMemberId());
        }

        savedLocationDAO.insertSavedLocation(savedLocationDTO);
    }

    /*
     * 기본 저장 위치 설정
     *
     * 한 회원당 기본 위치는 하나만 유지한다.
     */
    @Override
    @Transactional
    public void setDefaultLocation(Long memberId, Long locationId) {
        log.info("@# EvSavedLocationServiceImpl.setDefaultLocation()");
        log.info("@# memberId => {}", memberId);
        log.info("@# locationId => {}", locationId);

        savedLocationDAO.clearDefaultLocation(memberId);
        savedLocationDAO.setDefaultLocation(memberId, locationId);
    }

    /*
     * 저장 위치 삭제
     */
    @Override
    public void deleteSavedLocation(Long memberId, Long locationId) {
        log.info("@# EvSavedLocationServiceImpl.deleteSavedLocation()");
        log.info("@# memberId => {}", memberId);
        log.info("@# locationId => {}", locationId);

        savedLocationDAO.deleteSavedLocation(memberId, locationId);
    }
}
