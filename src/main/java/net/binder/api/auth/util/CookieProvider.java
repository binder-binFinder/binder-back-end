package net.binder.api.auth.util;

import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;

public class CookieProvider {
    
    public static Cookie getLoginCookie(String token) {
        Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setDomain("bin-finder.net");
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

    public static Cookie getLogoutCookie() {
        Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setDomain("bin-finder.net");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }
}
