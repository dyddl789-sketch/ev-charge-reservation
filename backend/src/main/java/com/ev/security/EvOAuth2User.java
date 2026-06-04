package com.ev.security;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ev.dto.member.EvMemberDTO;

public class EvOAuth2User extends EvUserDetails implements OAuth2User {

    private final Map<String, Object> attributes;

    public EvOAuth2User(EvMemberDTO evMemberDTO, Map<String, Object> attributes) {
        super(evMemberDTO);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(getMemberId());
    }
}