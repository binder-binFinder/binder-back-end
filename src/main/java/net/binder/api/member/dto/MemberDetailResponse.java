package net.binder.api.member.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import net.binder.api.common.dto.BaseResponse;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;

@Getter
public class MemberDetailResponse extends BaseResponse {

    private final String email;

    private final String nickname;

    private final Role role;

    @Builder
    public MemberDetailResponse(Long id, LocalDateTime createAt, LocalDateTime modifiedAt, String email,
                                String nickname, Role role) {
        super(id, createAt, modifiedAt);
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public static MemberDetailResponse from(Member member) {
        return MemberDetailResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .id(member.getId())
                .createAt(member.getCreateAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }
}
