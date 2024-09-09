package net.binder.api.member.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import net.binder.api.common.dto.BaseResponse;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;

@Getter
public class MemberProfile extends BaseResponse {

    private final String email;

    private final String nickname;

    private final Role role;

    private final String imageUrl;

    private final Long bookmarkCount;

    @Builder
    public MemberProfile(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt, String email,
                         String nickname, Role role, String imageUrl, Long bookmarkCount) {
        super(id, createdAt, modifiedAt);
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.imageUrl = imageUrl;
        this.bookmarkCount = bookmarkCount;
    }

    public static MemberProfile from(Member member, Long bookmarkCount) {
        return MemberProfile.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .imageUrl(member.getImageUrl())
                .bookmarkCount(bookmarkCount)
                .id(member.getId())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }
}
