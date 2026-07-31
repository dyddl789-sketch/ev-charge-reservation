<a id="top"></a>

# ⚡ EV Charge Reservation

<p align="center">
  <strong>전기차 충전 예약 및 최적화 웹 애플리케이션</strong>
</p>

<p align="center">
  차량 등록부터 충전소 검색, 충전기 선택, 예약 선점 및 충전 관리까지<br>
  전기차 이용자의 충전 과정을 하나의 서비스로 연결한 웹 프로젝트입니다.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white">
  <img src="https://img.shields.io/badge/PostGIS-3.6-008000?style=flat-square">
  <img src="https://img.shields.io/badge/Redis-5-DC382D?style=flat-square&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/MyBatis-Mapper-000000?style=flat-square">
</p>



<p align="center">
  <img src="docs/images/ev-charge-main-banner.png"
       alt="EV Charge Reservation"
       width="100%">
</p>

## 🧭 README 바로가기

<table>
  <thead>
    <tr>
      <th align="center">프로젝트 소개</th>
      <th align="center">핵심 구현</th>
      <th align="center">설계·화면</th>
      <th align="center">시연·문서</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center"><a href="#overview">📌 프로젝트 개요</a></td>
      <td align="center"><a href="#features">🚀 주요 구현 기능</a></td>
      <td align="center"><a href="#project-structure">📂 프로젝트 구조</a></td>
      <td align="center"><a href="#demo-videos">🎬 기능 시연 영상</a></td>
    </tr>
    <tr>
      <td align="center"><a href="#goals">📍 프로젝트 개발 목표</a></td>
      <td align="center"><a href="#stack-highlight">🛠 기술 스택 하이라이트</a></td>
      <td align="center"><a href="#backend-package">🧩 백엔드 패키지 구조</a></td>
      <td align="center"><a href="#presentation">🎤 최종 발표 자료</a></td>
    </tr>
    <tr>
      <td align="center"><a href="#service-flow">🔄 서비스 이용 흐름</a></td>
      <td align="center"><a href="#stack-detail">💻 기술 스택 상세</a></td>
      <td align="center"><a href="#resource-structure">📁 Resource 구조</a></td>
      <td align="center"><a href="#technical-document">📘 프로젝트 기술서</a></td>
    </tr>
    <tr>
      <td align="center"><a href="#experience">💡 프로젝트 경험</a></td>
      <td align="center"><a href="#data-storage">🗃 데이터 저장 기준</a></td>
      <td align="center"><a href="#design-artifacts">📁 프로젝트 설계 산출물</a></td>
      <td align="center"><a href="#screen-design">🖥 화면설계서</a></td>
    </tr>
    <tr>
      <td align="center"><a href="#release-history">📦 릴리즈 내역</a></td>
      <td align="center"><a href="#mybatis-setting">⚙️ MyBatis Mapper 설정</a></td>
      <td align="center"><a href="#database-script">🗄 데이터베이스 스크립트</a></td>
      <td align="center"><a href="#menu-structure">🗂 메뉴구조도</a></td>
    </tr>
    <tr>
      <td align="center"><a href="#top">⬆ 맨 위로</a></td>
      <td align="center"></td>
      <td align="center"><a href="#artifact-summary">📎 프로젝트 산출물 요약</a></td>
      <td align="center"><a href="#git-strategy">🌿 브랜치 전략 및 Git 규칙</a></td>
    </tr>
  </tbody>
</table>

> GitHub에서 항목을 클릭하면 해당 위치로 바로 이동합니다.

---

<a id="overview"></a>
## 📌 프로젝트 개요

| 구분 | 내용 |
|---|---|
| **프로젝트명** | EV Charge Reservation |
| **프로젝트 형태** | 전기차 충전 예약 및 최적화 웹 애플리케이션 |
| **개발 방식** | 팀 프로젝트 |
| **주요 역할** | 프로젝트 기획, 구조 설계, 기능 개발, 소스 통합 및 문서화 |
| **백엔드** | Java 17, Spring Boot 3.5, Spring MVC, MyBatis |
| **데이터베이스** | PostgreSQL 16, PostGIS 3.6 |
| **인증 및 실시간 처리** | Spring Security, OAuth2, Redis, WebSocket |
| **프론트엔드** | JSP, JavaScript, jQuery, AJAX, HTML, CSS |

---

<a id="goals"></a>
## 📍 프로젝트 개발 목표

### 전기차 충전 서비스의 전체 흐름 구현

사용자가 차량을 등록하고 주변 충전소와 충전기를 조회한 뒤, 원하는 시간에 충전 예약을 진행할 수 있도록 서비스 흐름을 구성했습니다.

### 중복 예약 방지 및 예약 안정성 확보

동일 충전기의 동일 시간대에 여러 사용자가 동시에 예약하는 문제를 줄이기 위해 Redis를 활용한 임시 예약 선점 구조를 적용했습니다.

### 위치 기반 충전소 검색

PostgreSQL과 PostGIS를 활용하여 사용자의 저장 위치와 충전소 간 거리를 계산하고, 차량 커넥터와 호환되는 충전소를 조회할 수 있도록 설계했습니다.

### 안전한 사용자 인증 및 권한 관리

Spring Security와 OAuth2를 활용하여 일반 로그인과 카카오 소셜 로그인을 처리하고, 사용자와 관리자 권한에 따라 접근 가능한 기능을 구분했습니다.

### 유지보수 가능한 계층형 구조 적용

Controller, Service, DAO, Mapper를 역할별로 분리하고 공통 네이밍 규칙과 패키지 구조를 적용하여 기능 확장과 유지보수가 쉽도록 구성했습니다.

---

<a id="features"></a>
## 🚀 주요 구현 기능

### 회원 및 인증

- 일반 회원가입 및 로그인
- BCrypt 기반 비밀번호 암호화
- Spring Security 인증·인가
- 카카오 OAuth2 소셜 로그인
- 이메일 인증번호 발송 및 확인
- 사용자·관리자 권한별 접근 제어
- 회원정보 및 프로필 이미지 수정

### 차량 관리

- 차량 모델 마스터 기반 차량 등록
- 차량 별칭 및 차량번호 관리
- 대표 차량 설정
- 사용자별 등록 차량 조회
- AJAX 기반 대표 차량 변경
- 차량 논리 삭제 및 이력 보존

### 충전소 및 충전기

- 충전소와 충전기 정보 조회
- PostGIS 기반 위치 및 거리 계산
- 사용자 기본 위치 기준 주변 충전소 검색
- 대표 차량 커넥터 타입 기반 충전기 조회
- 충전기 상태 및 요금 정보 제공
- 충전소 즐겨찾기 구조 설계

### 충전 예약

- 차량 및 충전기 선택
- 현재 배터리와 목표 배터리 입력
- 필요 충전량 계산
- 예상 충전 시간 및 비용 계산
- Redis 기반 예약 임시 선점
- 동일 시간대 중복 예약 방지
- 예약 상태 및 충전 내역 관리

### AI 챗봇

- Gemini API 기반 AI 챗봇
- 사용자별 채팅방 및 메시지 저장
- 이전 대화 복원
- Intent 기반 사용자 질문 분석
- 대표 차량 기준 충전소 추천
- 충전 시간 및 예상 비용 계산
- 실제 DB 조회 결과 기반 답변 생성

### 관리자 기능

- 관리자 대시보드
- 회원 목록 및 상세 조회
- 회원 상태 변경 및 복구
- 등록 차량·예약·충전 내역 조회
- 충전소 및 충전기 운영 현황 조회
- 예약과 매출 통계 확인

---

<a id="service-flow"></a>
## 🔄 서비스 이용 흐름

```text
회원가입 및 로그인
        ↓
차량 등록 및 대표 차량 설정
        ↓
기본 위치 또는 현재 위치 설정
        ↓
주변 충전소 검색
        ↓
차량과 호환되는 충전기 선택
        ↓
예약 시간 및 목표 충전량 입력
        ↓
Redis 기반 예약 임시 선점
        ↓
예약 가능 여부 확인
        ↓
PostgreSQL 예약 확정
        ↓
충전 진행 및 충전 내역 저장
```

---

<a id="stack-highlight"></a>
## 🛠 기술 스택 하이라이트

### Backend

`Java 17` `Spring Boot 3.5` `Spring MVC` `MyBatis`  
`Spring Security` `OAuth2 Client` `WebSocket`

### Frontend

`JSP` `JavaScript` `jQuery` `AJAX` `HTML5` `CSS3`

### Database / Cache

`PostgreSQL 16` `PostGIS 3.6` `Redis 5`

### External API

`Kakao OAuth2` `Gemini API`

### Development Tools

`STS 4.31` `DBeaver Community`  
`Another Redis Desktop Manager` `Git` `GitHub` `draw.io`

---

<a id="stack-detail"></a>
## 💻 기술 스택 상세

### 프론트엔드

| 기술 | 적용 내용 |
|---|---|
| **JSP** | Spring MVC 기반 서버 사이드 화면 구성 |
| **JavaScript** | 화면 이벤트 및 사용자 입력 처리 |
| **jQuery** | DOM 제어와 이벤트 처리 |
| **AJAX** | 페이지 새로고침 없는 비동기 요청 처리 |
| **HTML / CSS** | 사용자·관리자 화면 및 반응형 UI 구성 |

### 백엔드

| 기술 | 적용 내용 |
|---|---|
| **Java 17** | 프로젝트 주요 비즈니스 로직 구현 |
| **Spring Boot 3.5** | 애플리케이션 설정 및 내장 서버 환경 구성 |
| **Spring MVC** | Controller 중심의 요청·응답 처리 |
| **MyBatis** | SQL Mapper 기반 데이터 조회 및 처리 |
| **Spring Security** | 로그인, 권한 검증 및 URL 접근 제어 |
| **OAuth2** | 카카오 소셜 로그인 연동 |
| **WebSocket** | 실시간 채팅 및 알림 기능 확장 구조 |

### 데이터베이스 및 캐시

| 기술 | 적용 내용 |
|---|---|
| **PostgreSQL 16** | 회원, 차량, 충전소, 예약, 충전 이력 영구 저장 |
| **PostGIS 3.6** | 위치 정보 저장, 거리 계산 및 주변 충전소 검색 |
| **Redis 5** | 예약 임시 선점, 인증번호, 캐시 및 실시간 데이터 처리 |

---

<a id="data-storage"></a>
## 🗃 데이터 저장 기준

| 데이터 성격 | 저장 위치 |
|---|---|
| 회원, 차량, 충전소, 충전기 | PostgreSQL |
| 확정된 충전 예약 | PostgreSQL |
| 실제 충전 내역 및 통계 | PostgreSQL |
| 위치 좌표 및 거리 계산 | PostgreSQL + PostGIS |
| 예약 확정 전 임시 선점 | Redis |
| 이메일 인증번호 | Redis |
| 빠르게 변경되는 상태와 캐시 | Redis |

```text
PostgreSQL
= 반드시 보존해야 하는 서비스 원본 데이터

Redis
= 임시 데이터, 실시간 상태, 예약 선점 및 캐시
```

---

<a id="project-structure"></a>
## 📂 프로젝트 구조

```text
ev-charge-reservation/
├─ backend/                         # Spring Boot + JSP + MyBatis 소스
├─ database/                        # PostgreSQL DB 스크립트
├─ docs/                            # 프로젝트 문서 및 산출물
│  ├─ images/
│  │  ├─ ev_charging_system_architecture.drawio
│  │  ├─ ev-charge-erd.drawio
│  │  └─ 개선 업무흐름도 .drawio
│  │
│  ├─ ppt/
│  │  └─ 최종보고서 2팀.pdf
│  │
│  ├─ EV_Charge_메뉴구조도.xlsx
│  ├─ 프로젝트기술서.pdf
│  └─ 화면설계서.odp
│
├─ frontend/                        # 화면 설계 및 UI 프로토타입
├─ .gitignore
└─ README.md
```

---

<a id="backend-package"></a>
## 🧩 백엔드 패키지 구조

```text
com.ev
├─ config                           # Security, Web, Redis 등 환경 설정
├─ controller
│  ├─ admin                         # 관리자 요청 처리
│  └─ user                          # 사용자 요청 처리
├─ dao
│  ├─ admin                         # 관리자 데이터 접근
│  └─ user                          # 사용자 데이터 접근
├─ dto                              # 도메인별 데이터 전달 객체
├─ exception                        # 공통 예외 처리
├─ security                         # 인증 사용자와 로그인 처리
└─ service
   ├─ admin                         # 관리자 비즈니스 로직
   └─ user                          # 사용자 비즈니스 로직
```

---

<a id="resource-structure"></a>
## 📁 Resource 구조

```text
src/main/resources
├─ mybatis
│  └─ mappers
│     ├─ admin
│     └─ user
├─ static
│  ├─ css
│  ├─ images
│  └─ js
├─ application.properties
└─ application-secret.properties
```

```text
src/main/webapp/WEB-INF/views
├─ admin
├─ common
└─ user
```

---

<a id="mybatis-setting"></a>
## ⚙️ MyBatis Mapper 설정

```properties
mybatis.mapper-locations=classpath:/mybatis/mappers/**/*.xml
mybatis.type-aliases-package=com.ev.dto
```

DAO 인터페이스와 Mapper XML의 namespace를 일치시키고, 기능별 Mapper를 자동으로 탐색하도록 구성했습니다.

---

<a id="design-artifacts"></a>
<details>
<summary><strong>📁 프로젝트 설계 산출물</strong></summary>

<br>

### 📌 업무 흐름도

사용자의 회원가입부터 차량 등록, 충전소 검색, 예약 및 충전 완료까지의 업무 흐름을 정리한 문서입니다.

<p align="center">
  <a href="docs/images/개선%20업무흐름도.drawio.png">
    <img
      src="docs/images/개선%20업무흐름도.drawio.png"
      alt="EV Charge 업무 흐름도"
      width="100%">
  </a>
</p>

<p align="center">
  이미지를 클릭하면 원본 크기로 확인할 수 있습니다.
</p>

---

### 📌 시스템 아키텍처도

Spring Boot, PostgreSQL, PostGIS, Redis, 외부 API와 사용자 화면 사이의 연결 구조를 정리한 문서입니다.

<p align="center">
  <a href="docs/images/ev_charging_system_architecture.drawio.png">
    <img
      src="docs/images/ev_charging_system_architecture.drawio.png"
      alt="EV Charge 시스템 아키텍처도"
      width="100%">
  </a>
</p>

<p align="center">
  이미지를 클릭하면 원본 크기로 확인할 수 있습니다.
</p>

---

### 📌 ERD

회원, 차량 모델, 등록 차량, 충전소, 충전기, 예약, 충전 세션 및 AI 채팅 테이블의 관계를 정리한 문서입니다.

<p align="center">
  <a href="docs/images/ev-charge-erd.drawio.png">
    <img
      src="docs/images/ev-charge-erd.drawio.png"
      alt="EV Charge ERD"
      width="100%">
  </a>
</p>

<p align="center">
  이미지를 클릭하면 원본 크기로 확인할 수 있습니다.
</p>

</details>

---
<a id="demo-videos"></a>
<details>
<summary><strong>🎬 기능 시연 영상</strong></summary>

<br>

EV Charge Reservation의 회원 기능, 차량 관리, 충전 예약, 소셜 로그인 및 AI 챗봇 기능을 영상으로 확인할 수 있습니다.

<table>
  <tr>
    <td width="50%" align="center">
      <a href="https://youtu.be/yGGrbDCZpjM">
        <img
          src="https://img.youtube.com/vi/yGGrbDCZpjM/hqdefault.jpg"
          alt="회원가입 로그인 회원정보 변경 시연"
          width="100%">
      </a>
      <br>
      <strong>👤 회원가입·로그인·회원정보 변경</strong>
      <br>
      <sub>회원가입부터 로그인, 프로필 및 회원정보 변경까지의 전체 흐름</sub>
    </td>
    <td width="50%" align="center">
      <a href="https://youtu.be/LicAmI7OQ18">
        <img
          src="https://img.youtube.com/vi/LicAmI7OQ18/hqdefault.jpg"
          alt="카카오 소셜 로그인 시연"
          width="100%">
      </a>
      <br>
      <strong>🔐 카카오 소셜 로그인</strong>
      <br>
      <sub>Spring Security와 OAuth2 기반 카카오 로그인 및 회원 연동</sub>
    </td>
  </tr>

  <tr>
    <td width="50%" align="center">
      <a href="https://youtu.be/FBk7_kQA41A">
        <img
          src="https://img.youtube.com/vi/FBk7_kQA41A/hqdefault.jpg"
          alt="차량 등록 삭제 시연"
          width="100%">
      </a>
      <br>
      <strong>🚗 차량 등록·대표 차량 설정·삭제</strong>
      <br>
      <sub>차량 모델 선택, 차량 등록, 대표 차량 변경 및 논리 삭제 처리</sub>
    </td>
    <td width="50%" align="center">
      <a href="https://youtu.be/2Wy5XDkOfyo">
        <img
          src="https://img.youtube.com/vi/2Wy5XDkOfyo/hqdefault.jpg"
          alt="충전소 검색 예약 시연"
          width="100%">
      </a>
      <br>
      <strong>⚡ 충전소 검색 및 충전 예약</strong>
      <br>
      <sub>충전소와 충전기 조회, 예약 정보 입력 및 충전 예약 진행</sub>
    </td>
  </tr>

  <tr>
    <td colspan="2" align="center">
      <a href="https://youtu.be/fDlL7uip6bo">
        <img
          src="https://img.youtube.com/vi/fDlL7uip6bo/hqdefault.jpg"
          alt="EV Charge AI 챗봇 시연"
          width="60%">
      </a>
      <br>
      <strong>🤖 EV Charge AI 챗봇</strong>
      <br>
      <sub>사용자 질문 분석, 차량 정보 연동, 충전소 추천 및 충전 시간·비용 안내</sub>
    </td>
  </tr>
</table>

<br>

### 📋 영상별 주요 시연 내용

#### 1. 회원가입·로그인·회원정보 변경

- 회원가입 입력값 검증
- 아이디·닉네임·이메일·휴대폰 중복 확인
- 이메일 인증번호 발송 및 확인
- 프로필 이미지 등록 및 미리보기
- 일반 로그인 및 로그인 실패 메시지
- 닉네임·비밀번호·프로필 이미지 변경
- 변경된 회원정보의 화면 즉시 반영

<p>
  ▶ <a href="https://youtu.be/yGGrbDCZpjM"><strong>회원가입·로그인·회원정보 변경 영상 보기</strong></a>
</p>

---

#### 2. 카카오 소셜 로그인

- 카카오 OAuth2 로그인 요청
- 카카오 사용자 인증 및 동의
- 소셜 회원 정보 조회 또는 자동 등록
- Spring Security 인증 객체 생성
- 로그인 성공 후 서비스 화면 이동

<p>
  ▶ <a href="https://youtu.be/LicAmI7OQ18"><strong>카카오 소셜 로그인 영상 보기</strong></a>
</p>

---

#### 3. 차량 등록·대표 차량 설정·삭제

- 차량 모델 마스터 기반 차량 선택
- 차량 별칭 및 차량번호 등록
- 대표 차량 설정
- AJAX 기반 대표 차량 즉시 변경
- 차량 목록 및 대표 차량 정보 갱신
- 물리 삭제가 아닌 논리 삭제 적용

<p>
  ▶ <a href="https://youtu.be/FBk7_kQA41A"><strong>차량 등록·삭제 영상 보기</strong></a>
</p>

---

#### 4. 충전소 검색 및 충전 예약

- 사용자 위치 기준 충전소 조회
- 차량 커넥터와 호환되는 충전기 확인
- 충전기 상태와 충전 요금 확인
- 현재 배터리와 목표 배터리 입력
- 예상 충전량·시간·비용 계산
- 예약 가능 시간 확인 및 예약 진행
- Redis 기반 예약 선점과 중복 예약 방지

<p>
  ▶ <a href="https://youtu.be/2Wy5XDkOfyo"><strong>충전소 검색·예약 영상 보기</strong></a>
</p>

---

#### 5. EV Charge AI 챗봇

- 사용자별 AI 채팅방 및 대화 저장
- 이전 대화 불러오기
- 사용자 질문 Intent 분석
- 대표 차량과 기본 위치 정보 연동
- 차량에 맞는 주변 충전소 추천
- 현재·목표 배터리 기준 충전 시간 계산
- 충전기 요금을 활용한 예상 비용 계산
- 실제 DB 조회 결과 기반 AI 답변 생성

<p>
  ▶ <a href="https://youtu.be/fDlL7uip6bo"><strong>AI 챗봇 시연 영상 보기</strong></a>
</p>

</details>

---
<a id="presentation"></a>
<details>
<summary><strong>🎤 최종 발표 자료</strong></summary>

<br>

프로젝트 기획 배경, 시스템 구성, 주요 구현 기능, DB 설계 및 개발 결과를 정리한 최종 발표 자료입니다.

- [최종 발표 자료 보기](<docs/ppt/최종보고서_2팀.pdf>)

PDF 파일은 GitHub에서 바로 열거나 내려받을 수 있습니다.

</details>

---

<a id="technical-document"></a>
<details>
<summary><strong>📘 프로젝트 기술서</strong></summary>

<br>

프로젝트 개요, 사용 기술, 주요 기능, 구현 과정 및 프로젝트 결과를 정리한 기술 문서입니다.

- [프로젝트 기술서 보기](docs/프로그램기술서.pdf)

</details>

---

<a id="screen-design"></a>
<details>
<summary><strong>🖥 화면설계서</strong></summary>

<br>

사용자 화면과 관리자 화면의 구성, 입력 항목 및 화면 이동 흐름을 정리한 문서입니다.

- [화면설계서 내려받기](docs/화면설계서.odp)

> `.odp` 파일은 LibreOffice Impress 또는 Microsoft PowerPoint에서 열 수 있습니다.

</details>

---

<a id="menu-structure"></a>
<details>
<summary><strong>🗂 메뉴구조도</strong></summary>

<br>

사용자와 관리자 메뉴를 기능별로 구분하고 각 화면의 연결 관계를 정리한 문서입니다.

- [메뉴구조도 내려받기](docs/EV_Charge_메뉴구조도.xlsx)

> `.xlsx` 파일은 Microsoft Excel 또는 호환 프로그램에서 열 수 있습니다.

</details>

---

<a id="database-script"></a>
<details>
<summary><strong>🗄 데이터베이스 스크립트</strong></summary>

<br>

회원, 차량, 충전소, 충전기, 예약, 충전 세션, 즐겨찾기 및 AI 채팅 테이블 생성문을 포함합니다.

- [EV-CHARGE.SQL 보기](database/schema.sql)

### 주요 테이블

```text
app_member
saved_location
vehicle_model
vehicle
charging_station
charger
reservation
charging_session
favorite_station
ai_chat_room
ai_chat_message
```

</details>

---

<a id="git-strategy"></a>
<details>
<summary><strong>🌿 브랜치 전략 및 Git 규칙</strong></summary>

<br>

### 브랜치 구성

```text
main
develop
feature/member
feature/vehicle
feature/reservation
feature/station
feature/ai-chat
feature/admin-dashboard
```

### 작업 흐름

```text
기능별 브랜치 생성
        ↓
기능 구현 및 테스트
        ↓
Commit 및 Push
        ↓
Pull Request 작성
        ↓
코드 확인 및 충돌 해결
        ↓
develop 병합
        ↓
최종 검증 후 main 병합
```

### 커밋 메시지 예시

```bash
feat: implement reservation feature
fix: resolve duplicate reservation issue
refactor: improve vehicle service structure
docs: update README
chore: configure redis connection
```

</details>

---

<a id="release-history"></a>
<details>
<summary><strong>📦 릴리즈 내역</strong></summary>

<br>

| 버전 | 주요 내용 |
|---|---|
| **v1.0** | EV Charge Reservation 프로젝트 기본 완성 버전 |
| **v1.1** | 소스 코드는 v1.0을 유지하고 README 및 프로젝트 산출물을 정리한 문서 개선 버전 |
| **v1.11** | README 배너 아래 4열 분류형 목차와 섹션 바로가기를 추가한 문서 탐색 개선 버전 |

### v1.11 변경 사항

- README 배너 아래 `README 바로가기` 목차 추가
- 목차를 `프로젝트 소개 / 핵심 구현 / 설계·화면 / 시연·문서` 4개 영역으로 분류
- 각 목차 항목을 실제 README 섹션의 고정 앵커와 연결
- 프로젝트 개요, 주요 기능, 기술 스택, 설계 산출물, 시연 영상 및 문서로 즉시 이동 가능
- 기존 프로젝트 본문과 산출물 내용은 유지하고 문서 탐색 편의성 개선

### v1.1 변경 사항

- README 프로젝트 소개 개선
- 주요 기술 스택과 적용 목적 정리
- 핵심 구현 기능 설명 보완
- 프로젝트 구조 및 백엔드 패키지 구조 정리
- 업무 흐름도, 시스템 아키텍처도 및 ERD 연결
- 프로젝트 기술서 및 최종 발표 자료 연결
- 화면설계서와 메뉴구조도 연결
- 데이터베이스 SQL 문서 연결

</details>

---

<a id="artifact-summary"></a>
## 📎 프로젝트 산출물 요약

| 산출물 | 파일 |
|---|---|
| 업무 흐름도 | [개선 업무흐름도.drawio](<docs/images/개선 업무흐름도.drawio.png>) |
| 시스템 아키텍처도 | [ev_charging_system_architecture.drawio](docs/images/ev_charging_system_architecture.drawio.png) |
| ERD | [ev-charge-erd.drawio](docs/images/ev-charge-erd.drawio.png) |
| 최종 발표 자료 | [최종보고서 2팀.pdf](<docs/ppt/최종보고서_2팀.pdf>) |
| 프로젝트 기술서 | [프로젝트기술서.pdf](docs/프로그램기술서.pdf) |
| 화면설계서 | [화면설계서.odp](docs/화면설계서.odp) |
| 메뉴구조도 | [EV_Charge_메뉴구조도.xlsx](docs/EV_Charge_메뉴구조도.xlsx) |
| DB 스크립트 | [EV-CHARGE.SQL](database/schema.sql) |

---

<a id="experience"></a>
## 💡 프로젝트를 통해 경험한 내용

- Spring Boot 기반 계층형 웹 애플리케이션 설계
- Spring Security 및 OAuth2 인증 구조 적용
- PostgreSQL과 PostGIS를 활용한 위치 기반 데이터 처리
- Redis 기반 예약 선점 및 임시 데이터 관리
- MyBatis Mapper를 통한 SQL 중심 데이터 처리
- 사용자와 관리자 기능 분리
- AJAX 기반 화면 상태 즉시 반영
- AI API와 내부 DB를 연결한 사용자 맞춤형 답변 처리
- Git 브랜치와 Pull Request 기반 팀 협업
- 프로젝트 산출물 작성 및 GitHub 문서화

---

<p align="center">
  <strong>EV Charge Reservation</strong><br>
  전기차 이용자의 충전 탐색부터 예약과 충전 관리까지 연결한 통합 서비스
</p>