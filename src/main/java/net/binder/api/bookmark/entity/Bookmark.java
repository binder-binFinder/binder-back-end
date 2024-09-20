package net.binder.api.bookmark.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.entity.BaseEntity;
import net.binder.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "sameBookmark",
                        columnNames = {"member_id", "bin_id"}
                )
        }
)
public class Bookmark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @Builder
    public Bookmark(Member member, Bin bin) {
        this.member = member;
        this.bin = bin;
    }
}
