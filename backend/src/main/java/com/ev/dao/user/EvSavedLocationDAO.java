package com.ev.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.map.EvSavedLocationDTO;

/*
 * 저장 위치 DAO
 *
 * MyBatis Mapper XML과 연결되는 인터페이스다.
 * namespace는 com.ev.dao.user.EvSavedLocationDAO 로 맞춰야 한다.
 */
@Mapper
public interface EvSavedLocationDAO {

    /*
     * 로그인 회원의 저장 위치 목록 조회
     *
     * 기본 위치(is_default=true)를 먼저 보여주기 위해 Mapper에서 정렬한다.
     */
    List<EvSavedLocationDTO> findSavedLocationList(@Param("memberId") Long memberId);

    /*
     * 저장 위치 등록
     *
     * latitude / longitude는 DTO로 받고,
     * PostGIS location 컬럼은 Mapper XML에서 ST_MakePoint로 생성한다.
     */
    void insertSavedLocation(EvSavedLocationDTO savedLocationDTO);

    /*
     * 해당 회원의 기본 위치를 모두 해제한다.
     *
     * 기본 위치는 한 회원당 하나만 유지하기 위해
     * setDefaultLocation() 전에 먼저 실행한다.
     */
    void clearDefaultLocation(@Param("memberId") Long memberId);

    /*
     * 특정 저장 위치를 기본 위치로 설정한다.
     */
    void setDefaultLocation(@Param("memberId") Long memberId,
                            @Param("locationId") Long locationId);

    /*
     * 저장 위치 삭제
     *
     * memberId 조건을 같이 걸어서 다른 회원의 위치를 삭제하지 못하게 한다.
     */
    void deleteSavedLocation(@Param("memberId") Long memberId,
                             @Param("locationId") Long locationId);
}
