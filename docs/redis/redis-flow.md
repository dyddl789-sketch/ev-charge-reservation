# Redis Flow

# 1. 충전기 예약 흐름

```text
사용자 예약 요청
→ Redis 예약 선점 확인
→ PostgreSQL 예약 저장
→ Redis 충전기 상태 변경
→ WebSocket 실시간 전송
```

---

# 2. 예약 선점 흐름

```text
사용자 A 예약 시도
→ reservation:hold 생성
→ 동일 시간 예약 차단
→ 예약 완료 후 hold 제거
```

---

# 3. 충전기 상태 흐름

```text
사용가능
→ 예약중
→ 사용중
→ 사용가능
```

예외 상태:

```text
점검중
고장
```

---

# 4. 예약 인증 흐름

```text
예약 완료
→ reservation:auth 생성
→ 사용자 도착
→ 인증 코드 입력
→ 인증 성공
→ 충전 시작
```

---

# 5. 노쇼 처리 흐름

```text
예약 완료
→ reservation:expire 생성
→ 인증 대기
→ 제한 시간 초과
→ 노쇼 처리
→ 충전기 상태 초기화
```

---

# 6. 실시간 충전 상태 흐름

```text
충전 시작
→ charging:session 생성
→ 실시간 배터리 상태 변경
→ 충전 완료
→ charging:session 제거
```

---

# 7. 차량 이동 시뮬레이션 흐름

```text
예약 완료
→ 길찾기 API 경로 조회
→ simulation:location 생성
→ 차량 마커 이동
→ 충전소 도착
```

---

# 8. WebSocket 흐름

```text
Redis 상태 변경
→ WebSocket 이벤트 발생
→ 사용자 화면 상태 변경
```
