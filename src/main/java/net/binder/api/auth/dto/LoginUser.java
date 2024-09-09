package net.binder.api.auth.dto;

import lombok.Builder;
import lombok.Getter;
import net.binder.api.member.entity.Member;

@Getter
public class LoginUser {

    private final String email;

    private final String role;

    @Builder
    public LoginUser(String email, String role) {
        this.email = email;
        this.role = role;
    }

    public static LoginUser from(Member member) {
        return LoginUser.builder()
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();
    }

}
