package com.ev.security;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ev.dao.user.EvMemberDAO;
import com.ev.dto.member.EvMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvOAuth2UserService extends DefaultOAuth2UserService {

    private final EvMemberDAO evMemberDAO;

    private static final String DEFAULT_PROFILE_IMAGE_URL =
            "/images/member/profile/default-profile.png";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        log.info("@# EvOAuth2UserService.loadUser()");

        OAuth2User oauth2User = super.loadUser(userRequest);
        log.info("@# oauth2User attributes => {}", oauth2User.getAttributes());

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        if (!"kakao".equals(registrationId)) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }

        Map<String, Object> attributes = oauth2User.getAttributes();

        Long kakaoId = Long.valueOf(String.valueOf(attributes.get("id")));

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");

        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = String.valueOf(profile.get("nickname"));
        String profileImageUrl = (String) profile.get("profile_image_url");

        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;
        }

        String userId = "kakao_" + kakaoId;

        // 카카오 동의항목에서 account_email을 수집하도록 설정한 경우 실제 이메일 조회
        String email = (String) kakaoAccount.get("email");

        // 이메일이 없는 경우를 대비한 fallback
        if (email == null || email.isBlank()) {
            email = userId + "@kakao.oauth.local";
        }

        log.info("@# kakao email => {}", email);
        log.info("@# kakao nickname => {}", nickname);
        log.info("@# kakao profileImageUrl => {}", profileImageUrl);

        EvMemberDTO evMemberDTO = evMemberDAO.findByUserId(userId);

        if (evMemberDTO == null) {
            evMemberDTO = new EvMemberDTO();
            evMemberDTO.setUserId(userId);
            evMemberDTO.setPassword(null);
            evMemberDTO.setMemberName(nickname);
            evMemberDTO.setNickname(nickname);
            evMemberDTO.setEmail(email);
            evMemberDTO.setProfileImageUrl(profileImageUrl);
            evMemberDTO.setUserType("USER");
            evMemberDTO.setLoginType("KAKAO");
            evMemberDTO.setStatus("ACTIVE");

            evMemberDAO.insertSocialMember(evMemberDTO);

            evMemberDTO = evMemberDAO.findByUserId(userId);
        }

        return new EvOAuth2User(evMemberDTO, attributes);
    }
}