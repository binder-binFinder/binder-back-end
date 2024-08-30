package net.binder.api.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public CustomAuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getName();
        String role = oAuth2User.getRole();

        String token = jwtUtil.generateToken(email, role);
        String redirectUri = getRedirectUri(request); // 개발 환경이므로 해당 방식으로 리다이렉트

        String uriString = getUriString(redirectUri, token);

        response.sendRedirect(uriString);
    }

    private String getRedirectUri(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.REFERER) + "/auth/result";
    }

    private String getUriString(String redirectUri, String token) {
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam(HttpHeaders.AUTHORIZATION, token)
                .build()
                .toUriString();
    }
}
