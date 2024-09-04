package net.binder.api.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.dto.LoginUser;
import net.binder.api.auth.util.CookieProvider;
import net.binder.api.auth.util.JwtUtil;
import net.binder.api.common.util.ErrorResponseUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JwtFilter Start");
        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            Authentication authentication = getAuthentication(username, role);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            ErrorResponseUtil.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", objectMapper);
        }
    }

    private static String extractToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        Cookie cookie = CookieProvider.getAuthorizationCookie(request);

        if (cookie == null) {
            return null;
        }

        return cookie.getValue();
    }


    private Authentication getAuthentication(String username, String role) {
        LoginUser loginUser = new LoginUser(username, role);

        OAuth2User oAuth2User = new CustomOAuth2User(loginUser);

        return new UsernamePasswordAuthenticationToken(
                oAuth2User, null, oAuth2User.getAuthorities());
    }
}
