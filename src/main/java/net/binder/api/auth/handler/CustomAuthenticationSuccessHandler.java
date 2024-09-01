package net.binder.api.auth.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REDIRECT_URI = "https://www.bin-finder.net";
    public static final String DOMAIN = "www.bin-finder.net";

    private final JwtUtil jwtUtil;


    public CustomAuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getName();
        String role = oAuth2User.getRole();

        String token = jwtUtil.generateToken(email, role);

        response.addCookie(getCookie(token));
        response.sendRedirect(REDIRECT_URI);

    }

    private Cookie getCookie(String token) {
        Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setDomain(".bin-finder.net");  // 점(.)을 앞에 추가하여 서브도메인 포함
        cookie.setMaxAge(3600);  // 1시간 유효
        cookie.setAttribute("SameSite", "None");  // 크로스 사이트 요청 허용
        return cookie;
    }
}
