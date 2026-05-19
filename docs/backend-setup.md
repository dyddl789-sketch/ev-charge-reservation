# Backend Environment Setup

Spring Boot + JSP + PostgreSQL + Redis + MyBatis 기반 백엔드 개발 환경 설정 문서

---

# Development Environment

- STS 4.31
- Java 17
- Spring Boot 3.5.14
- PostgreSQL 16.14
- PostGIS 3.6
- Redis 5.0.14
- Embedded Apache Tomcat 10.1.54
- DBeaver Community

---

# application.properties

```properties
spring.application.name=backend

# JSP
server.port=8383
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp

# MyBatis
mybatis.mapper-locations=classpath:/mybatis/mappers/**/*.xml
mybatis.type-aliases-package=com.ev.dto

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/ev_charge
spring.datasource.username=postgres
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Encoding
server.servlet.encoding.force=true
```

---

# MyBatis Mapper Scan

```properties
mybatis.mapper-locations=classpath:/mybatis/mappers/**/*.xml
```

## Description

Spring Boot가 `mybatis/mappers` 폴더 아래의 모든 XML Mapper 파일을 자동으로 탐색 및 등록한다.

예시:

```text
resources
└─ mybatis
   └─ mappers
      ├─ member-mapper.xml
      ├─ reservation-mapper.xml
      └─ chat-mapper.xml
```

---

# RedisConfig

```java
package com.ev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    // Redis 문자열 저장용 Template Bean 등록
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {

        return new StringRedisTemplate(connectionFactory);
    }
}
```

## Description

- Spring Boot와 Redis 연결 설정
- Redis 문자열 저장 및 조회용 Template Bean 등록
- Redis Pub/Sub 및 캐시 기능 기반 설정

---

# SecurityConfig

```java
package com.ev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // 모든 요청 허용
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // Spring Security 기본 로그인 페이지 비활성화
            .formLogin(form -> form.disable());

        return http.build();
    }
}
```

## Description

- Spring Security 기본 보안 설정
- 개발 초기 단계에서 모든 요청 허용
- Spring 기본 로그인 화면 제거
- CSRF 비활성화 상태

---

# build.gradle

```gradle
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.5.14'
	id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.ev'
version = '0.0.1-SNAPSHOT'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation 'org.springframework.boot:spring-boot-starter-data-redis'
	implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
	implementation 'org.springframework.boot:spring-boot-starter-security'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-websocket'

	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5'

	// JSP
	implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
	implementation 'jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api'
	implementation 'org.glassfish.web:jakarta.servlet.jsp.jstl'

	compileOnly 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'

	runtimeOnly 'org.postgresql:postgresql'

	annotationProcessor 'org.projectlombok:lombok'

	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.5'
	testImplementation 'org.springframework.security:spring-security-test'

	testCompileOnly 'org.projectlombok:lombok'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
	testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

---

# Dependency Description

| Dependency | Description |
|---|---|
| Spring Web | REST API 및 MVC |
| Spring Security | 인증 및 보안 |
| OAuth2 Client | 소셜 로그인 |
| WebSocket | 실시간 통신 |
| Redis | 캐시 및 Pub/Sub |
| PostgreSQL | 메인 데이터베이스 |
| MyBatis | SQL Mapper |
| JSP / JSTL | View 렌더링 |
| Lombok | 코드 간소화 |
| DevTools | 자동 재시작 |

---

# Current Setup Status

- JSP 연결 완료
- Spring Security 설정 완료
- Redis 연결 완료
- PostgreSQL 연결 완료
- DBeaver 연결 완료
- MyBatis XML Mapper Scan 설정 완료
- Embedded Tomcat 정상 동작 확인 완료