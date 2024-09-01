package net.binder.api.auth.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.dto.GoogleResponse;
import net.binder.api.auth.dto.KakaoResponse;
import net.binder.api.auth.dto.LoginUser;
import net.binder.api.auth.dto.NaverResponse;
import net.binder.api.auth.dto.OAuth2Response;
import net.binder.api.member.entity.Member;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialMemberService socialMemberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = super.loadUser(userRequest).getAttributes();

        OAuth2Response oAuth2Response = getOAuth2Response(registrationId, attributes);

        if (oAuth2Response == null) {
            return null;
        }

        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();

        Member member = socialMemberService.findBySocialAccountOrEmail(provider, providerId, email);

        if (member == null) {
            member = socialMemberService.register(provider, providerId, email, oAuth2Response.getName());
        }

        return new CustomOAuth2User(LoginUser.from(member));
    }

    private OAuth2Response getOAuth2Response(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equals("google")) {
            return new GoogleResponse(attributes);
        }
        if (registrationId.equals("naver")) {
            return new NaverResponse(attributes);
        }
        if (registrationId.equals("kakao")) {
            return new KakaoResponse(attributes);
        }
        return null;
    }
}
