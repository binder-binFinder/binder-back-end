package net.binder.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {

        Cookie cookie = new Cookie(HttpHeaders.AUTHORIZATION, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setDomain("bin-finder.net");

        response.addCookie(cookie);
    }
}
