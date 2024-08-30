package net.binder.api.auth.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final LoginUser loginUser;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = loginUser.getRole();

        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getName() {
        return loginUser.getNickname();
    }

    public String getEmail() {
        return loginUser.getEmail();
    }

    public String getRole() {
        return loginUser.getRole();
    }
}
