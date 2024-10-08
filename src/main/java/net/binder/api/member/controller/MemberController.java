package net.binder.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.util.CookieProvider;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.member.dto.BinRegistrationActivity;
import net.binder.api.member.dto.MemberDeleteRequest;
import net.binder.api.member.dto.MemberProfile;
import net.binder.api.member.dto.MemberProfileUpdateRequest;
import net.binder.api.member.service.MemberService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
@Tag(name = "회원 관리")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "본인 정보 조회")
    @GetMapping("/me")
    public MemberProfile profile(@CurrentUser String email) {
        return memberService.getProfile(email);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public void delete(@CurrentUser String email, MemberDeleteRequest request, HttpServletResponse response) {
        memberService.deleteMember(email, request.getInput());

        Cookie cookie = CookieProvider.getLogoutCookie();

        response.addCookie(cookie);
    }

    @Operation(summary = "회원 프로필 수정")
    @PatchMapping("/me")
    public void updateProfile(@CurrentUser String email, MemberProfileUpdateRequest request) {
        memberService.updateProfile(email, request.getNickname(), request.getImageUrl());
    }

    @Operation(summary = "회원 타임라인 조회")
    @GetMapping("/me/timeline")
    public List<BinRegistrationActivity> timeline(@CurrentUser String email,
                                                  @RequestParam(required = false) Long lastBinId) {
        return memberService.getRegistrationActivities(email, lastBinId);
    }
}
