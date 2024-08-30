package net.binder.api.member.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

    private String provider;

    private String providerId;

    @Builder
    public SocialAccount(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
}
