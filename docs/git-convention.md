# Git Commit Message Convention

## 기본 규칙

```bash
타입: 작업내용
```

예시:

```bash
feat: add login feature
fix: resolve reservation bug
docs: update README
```

---

# Commit Type 규칙

| 타입       | 설명          |
| -------- | ----------- |
| feat     | 새로운 기능 추가   |
| fix      | 버그 수정       |
| refactor | 코드 리팩토링     |
| style    | UI/CSS 수정   |
| docs     | 문서 수정       |
| chore    | 설정 및 환경 수정  |
| test     | 테스트 코드      |
| build    | 빌드 관련 수정    |
| rename   | 파일/폴더 이름 변경 |
| remove   | 파일/코드 삭제    |

---

# Commit Message 작성 규칙

## 1. 영어 소문자 사용

```bash
feat: add redis config
```

✅

```bash
Feat: Add Redis Config
```

❌

---

## 2. 한 작업 단위로 commit

좋은 예:

```bash
feat: implement oauth2 login
```

나쁜 예:

```bash
로그인도하고 css도 수정하고 db도 수정
```

❌

---

## 3. commit message는 간결하게 작성

좋은 예:

```bash
fix: resolve websocket connection issue
```

나쁜 예:

```bash
fix: websocket connection issue because server was not running correctly
```

❌

---

# Commit Message 예시

## 프로젝트 초기 설정

```bash
feat: initialize backend project
```

```bash
chore: configure application yml
```

```bash
build: add redis dependency
```

---

# 회원 기능

```bash
feat: implement signup feature
```

```bash
feat: implement login feature
```

```bash
feat: add oauth2 login
```

```bash
fix: resolve login validation error
```

---

# 예약 기능

```bash
feat: add reservation service
```

```bash
feat: implement reservation api
```

```bash
fix: resolve duplicate reservation issue
```

---

# Redis / WebSocket

```bash
feat: implement websocket chat
```

```bash
feat: add redis pubsub config
```

```bash
fix: resolve redis connection issue
```

---

# DB / MyBatis

```bash
feat: create reservation mapper
```

```bash
feat: add charging station table
```

```bash
refactor: optimize reservation query
```

---

# JSP / 화면

```bash
style: update main page ui
```

```bash
style: improve reservation page design
```

```bash
feat: add login jsp page
```

---

# 문서 작업

```bash
docs: update README
```

```bash
docs: add erd document
```

```bash
docs: write api specification
```

---

# 테스트

```bash
test: add reservation service test
```

```bash
test: verify redis connection
```

---

# 금지 사항

## 의미 없는 commit message 금지

```bash
수정
최종
진짜최종
123
test
```

❌

---

# 추천 Git Workflow

```bash
기능 구현
→ git add .
→ git commit
→ git push
```

작업 단위별로 자주 commit 하기.

---

# Branch 전략 (추후 사용 예정)

```bash
main
feature/login
feature/chat
feature/reservation
```

예시:

```bash
feature/oauth-login
feature/websocket-chat
feature/redis-config
```
