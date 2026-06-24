package com.ev.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvJwtRedisTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    public void saveRefreshToken(Long memberId, String refreshToken, long expirationMs) {
        String key = REFRESH_PREFIX + memberId;
        log.info("@# saveRefreshToken key => {}", key);

        stringRedisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofMillis(expirationMs)
        );
    }

    public String getRefreshToken(Long memberId) {
        String key = REFRESH_PREFIX + memberId;
        log.info("@# getRefreshToken key => {}", key);
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_PREFIX + memberId;
        log.info("@# deleteRefreshToken key => {}", key);
        stringRedisTemplate.delete(key);
    }

    public void addBlackList(String accessToken, long remainingMs) {
        if (accessToken == null || accessToken.isBlank() || remainingMs <= 0) {
            return;
        }

        String key = BLACKLIST_PREFIX + accessToken;
        log.info("@# addBlackList remainingMs => {}", remainingMs);

        stringRedisTemplate.opsForValue().set(
                key,
                "logout",
                Duration.ofMillis(remainingMs)
        );
    }

    public boolean isBlackListed(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
