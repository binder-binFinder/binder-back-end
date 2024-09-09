package net.binder.api.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.util.CookieProvider;
import net.binder.api.auth.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REDIRECT_URI = "https://www.bin-finder.net";

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

        response.addCookie(CookieProvider.getLoginCookie(token));
        response.sendRedirect(REDIRECT_URI);
    }

}
