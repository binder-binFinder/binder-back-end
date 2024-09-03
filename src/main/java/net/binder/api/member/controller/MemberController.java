package net.binder.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.auth.util.CookieProvider;
import net.binder.api.member.dto.MemberDetailResponse;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
@Tag(name = "회원 관리")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "본인 정보 조회")
    @GetMapping("/me")
    public MemberDetailResponse profile(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String email = customOAuth2User.getName();

        Member member = memberService.findByEmail(email);
        long likeCount = memberService.calculateLikeCount(member);

        return MemberDetailResponse.from(member, likeCount);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public void delete(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, HttpServletResponse response) {
        String email = customOAuth2User.getName();

        memberService.deleteMember(email);

        Cookie cookie = CookieProvider.getLogoutCookie();

        response.addCookie(cookie);
    }
}
