package net.binder.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberProfileUpdateRequest {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private final String nickname;

    private final String imageUrl;
}
