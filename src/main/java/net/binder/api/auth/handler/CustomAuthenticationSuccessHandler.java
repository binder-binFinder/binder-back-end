package net.binder.api.auth.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    private final String redirectUri;

    public CustomAuthenticationSuccessHandler(JwtUtil jwtUtil,
                                              @Value("${spring.security.oauth2.redirect-uri}") String redirectUri) {
        this.jwtUtil = jwtUtil;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getName();
        String role = oAuth2User.getRole();

        String token = jwtUtil.generateToken(email, role);

        response.addCookie(getCookie(token));
        response.sendRedirect(redirectUri);

    }

    private Cookie getCookie(String token) {
        Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        return cookie;
    }
}
