# EV Charge Reservation

전기차 충전 예약 및 최적화 시스템

---

## Tech Stack

* Spring Boot 3.5
* Java 17
* PostgreSQL 16
* PostGIS 3.6
* Redis
* MyBatis
* WebSocket
* Spring Security
* OAuth2

---

## Project Structure

```text
ev-charge-reservation/
├─ backend/          # Spring Boot + JSP + MyBatis 실제 코드
├─ database/         # DB 스크립트
├─ redis/            # Redis 설정 문서
├─ docs/             # 프로젝트 문서
└─ frontend/         # 화면 설계 및 UI 프로토타입
```

---

## Documents

* [Git Convention](docs/git-convention.md)
* [API Specification](docs/api-spec.md)
* [ERD](docs/erd.md)
* [Meeting Notes](docs/meeting-notes.md)

---

## Main Features

* 전기차 충전소 검색
* 충전 예약 시스템
* 충전 시간 최적화 추천
* 실시간 채팅 및 알림
* 소셜 로그인(OAuth2)
* Redis 기반 캐시 및 Pub/Sub
* WebSocket 기반 실시간 통신
