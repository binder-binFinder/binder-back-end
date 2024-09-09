package net.binder.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.util.CookieProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "인증 관리")
public class AuthController {

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {

        Cookie cookie = CookieProvider.getLogoutCookie();

        response.addCookie(cookie);
    }

}
