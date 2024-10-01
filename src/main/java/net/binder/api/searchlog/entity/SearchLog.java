package net.binder.api.searchlog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;
import net.binder.api.common.entity.BaseEntityWithSoftDelete;
import net.binder.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog extends BaseEntityWithSoftDelete {

    private Long id;

    private String keyword;

    private String address;

    private boolean hasBookmarkedBin;

    private boolean hasBinsNearby;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public SearchLog(Long id, String keyword, String address, boolean hasBookmarkedBin, boolean hasBinsNearby, Member member) {
        this.id = id;
        this.keyword = keyword;
        this.address = address;
        this.hasBookmarkedBin = hasBookmarkedBin;
        this.hasBinsNearby = hasBinsNearby;
        this.member = member;
    }

    public boolean isOwnedBy(String email){
        return member.getEmail().equals(email);
    }
}
