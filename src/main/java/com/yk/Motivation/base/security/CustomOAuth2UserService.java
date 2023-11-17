package com.yk.Motivation.base.security;

import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // 요청을 토대로 OAuth2 사용자 로드

        String oauthId = oAuth2User.getName();
        Map<String, Object> attributes = oAuth2User.getAttributes(); // OAuth2User 에서  Map 형태로 attribute 가져옴

        Map attributesProperties = (Map) attributes.get("properties"); // attribute 에서 properties 의 value Map 형태로 가져옴
        String nickname = (String) attributesProperties.get("nickname"); // properties 에서 nickname 의 value 가져옴
        String profileImgUrl = (String) attributesProperties.get("profile_image"); // properties 에서 profile_image 의 value 가져옴

        System.out.println("profile_image : " + profileImgUrl);

        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // provider 의 Id 대문자로 가져옴 ( KAKAO )

        String username = providerTypeCode + "__%s".formatted(oauthId); // (KAKAO__oauthId)

        Member member = memberService.whenSocialLogin(providerTypeCode, username, nickname, profileImgUrl); // 현재 회원 인지 조회, 없으면 가입

        return new CustomOAuth2User(member.getUsername(), member.getPassword(), member.getGrantedAuthorities());
    }
}

class CustomOAuth2User extends User implements OAuth2User {

    public CustomOAuth2User(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}