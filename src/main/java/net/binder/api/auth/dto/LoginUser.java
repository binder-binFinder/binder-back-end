package net.binder.api.auth.dto;

import lombok.Builder;
import lombok.Getter;
import net.binder.api.member.entity.Member;

@Getter
public class LoginUser {

    private final String email;

    private final String nickname;

    private final String role;

    @Builder
    public LoginUser(String email, String nickname, String role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public static LoginUser from(Member member) {
        return LoginUser.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole().name())
                .build();
    }

}
