package net.binder.api.auth.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.dto.GoogleResponse;
import net.binder.api.auth.dto.LoginUser;
import net.binder.api.auth.dto.OAuth2Response;
import net.binder.api.member.entity.Member;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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

        Member member = socialMemberService.getMember(oAuth2Response.getProvider(),
                oAuth2Response.getProviderId(),
                oAuth2Response.getEmail(),
                oAuth2Response.getName());

        return new CustomOAuth2User(LoginUser.from(member));
    }

    private OAuth2Response getOAuth2Response(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equals("google")) {
            return new GoogleResponse(attributes);
        }
        return null;
    }
}
