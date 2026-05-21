# Redis Usage

# Redis 사용 목적

본 프로젝트는 Redis를 사용하여 실시간 상태 관리 및 빠른 데이터 처리를 수행한다.

---

# Redis 사용 기능

## 1. 충전기 실시간 상태 관리

사용 데이터:

```text
charger:status:{chargerId}
```

관리 상태:

```text
사용가능
예약중
사용중
점검중
고장
```

---

# 2. 예약 임시 선점

사용 데이터:

```text
reservation:hold:{chargerId}:{startTime}
```

목적:

```text
동시에 같은 충전기 예약 방지
```

---

# 3. 예약 인증 코드 관리

사용 데이터:

```text
reservation:auth:{reservationId}
```

목적:

```text
충전기 예약 인증 처리
```

---

# 4. 노쇼 처리 관리

사용 데이터:

```text
reservation:expire:{reservationId}
```

목적:

```text
예약 인증 제한 시간 관리
```

---

# 5. 실시간 충전 상태 관리

사용 데이터:

```text
charging:session:{sessionId}
```

목적:

```text
실시간 배터리 상태 및 충전 진행 상태 관리
```

---

# 6. 차량 이동 시뮬레이션

사용 데이터:

```text
simulation:location:{memberId}
```

목적:

```text
지도 기반 차량 이동 시뮬레이션
```

---

# 7. WebSocket Pub/Sub

사용 데이터:

```text
ws:charger:{chargerId}
chat:room:{roomId}
```

목적:

```text
실시간 상태 및 채팅 데이터 전송
```

---

# 8. 로그인 토큰 관리

사용 데이터:

```text
auth:refresh:{memberId}
auth:blacklist:{accessToken}
```

목적:

```text
로그인 유지 및 로그아웃 토큰 관리
```

---

# Redis 사용 이유

Redis는 메모리 기반 저장소로 매우 빠른 속도를 제공한다.

본 프로젝트에서는:

* 실시간 상태 관리
* 예약 임시 잠금
* 실시간 채팅
* WebSocket Pub/Sub
* 위치 시뮬레이션

과 같은 빠른 처리가 필요한 기능에 사용한다.

---

# PostgreSQL과 Redis 역할 분리

## PostgreSQL

```text
영구 저장 데이터
예약 정보
회원 정보
충전 이력
```

---

# Redis

```text
실시간 상태 데이터
임시 데이터
캐시 데이터
Pub/Sub 데이터
```
