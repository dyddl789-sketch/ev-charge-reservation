package com.ev.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ev.dto.member.EvMemberDTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EvUserDetails implements UserDetails {
	
	private static final long serialVersionUID = 1L;

    private final EvMemberDTO evMemberDTO;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + evMemberDTO.getUserType()));
    }

    @Override
    public String getPassword() {
        return evMemberDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return evMemberDTO.getUserId();
    }

    public Long getMemberId() {
        return evMemberDTO.getMemberId();
    }

    public String getMemberName() {
        return evMemberDTO.getMemberName();
    }

    public String getUserType() {
        return evMemberDTO.getUserType();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return "ACTIVE".equals(evMemberDTO.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(evMemberDTO.getStatus());
    }
}