# Redis Key Naming Rules

## 기본 규칙

Redis Key는 아래 규칙으로 작성한다.

```text
domain:feature:identifier
```

예시:

```text
charger:status:1
reservation:hold:1:202605211400
```

---

# 1. 충전기 상태

## Key

```text
charger:status:{chargerId}
```

## Example

```text
charger:status:1 = 사용중
```

## Description

충전기의 현재 상태를 저장한다.

상태 값:

```text
사용가능
예약중
사용중
점검중
고장
```

---

# 2. 예약 임시 선점

## Key

```text
reservation:hold:{chargerId}:{startTime}
```

## Example

```text
reservation:hold:1:202605211400
```

## Description

동시에 같은 충전기를 예약하는 것을 방지하기 위한 임시 선점 데이터.

TTL(Time To Live):

```text
3 ~ 5분
```

---

# 3. 예약 인증 코드

## Key

```text
reservation:auth:{reservationId}
```

## Example

```text
reservation:auth:10 = R8291
```

## Description

예약 인증 코드 저장.

TTL:

```text
예약 종료 또는 노쇼 처리 시 삭제
```

---

# 4. 노쇼 만료 관리

## Key

```text
reservation:expire:{reservationId}
```

## Example

```text
reservation:expire:10
```

## Description

예약 인증 제한 시간 관리.

TTL:

```text
예약 시작 후 15분
```

---

# 5. 충전 진행 상태

## Key

```text
charging:session:{sessionId}
```

## Example

```text
charging:session:5
```

## Description

실시간 충전 상태 저장.

예시 데이터:

```text
배터리 잔량
남은 충전 시간
충전 상태
```

---

# 6. 차량 이동 시뮬레이션

## Key

```text
simulation:location:{memberId}
```

## Example

```text
simulation:location:3
```

## Description

차량 이동 시뮬레이션 현재 위치 저장.

---

# 7. WebSocket 충전기 채널

## Key

```text
ws:charger:{chargerId}
```

## Description

충전기 상태 실시간 전송 채널.

---

# 8. 채팅방 Pub/Sub

## Key

```text
chat:room:{roomId}
```

## Description

실시간 채팅 메시지 전달.

---

# 9. Refresh Token

## Key

```text
auth:refresh:{memberId}
```

## Description

로그인 유지용 Refresh Token 저장.

---

# 10. Access Token Blacklist

## Key

```text
auth:blacklist:{accessToken}
```

## Description

로그아웃 처리된 Access Token 관리.
