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

    private final String image_url;

    @Builder
    public MemberDetailResponse(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt, String email,
                                String nickname, Role role, String imageUrl) {
        super(id, createdAt, modifiedAt);
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.image_url = imageUrl;
    }

    public static MemberDetailResponse from(Member member) {
        return MemberDetailResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .imageUrl(member.getImage_url())
                .id(member.getId())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }
}
