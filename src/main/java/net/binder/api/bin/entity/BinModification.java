package net.binder.api.bin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;
import net.binder.api.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BinModification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;

    private String title;

    private String address;

    @Enumerated(EnumType.STRING)
    private BinType type;

    private String imageUrl;

    private double latitude;

    private double longitude;

    @Enumerated(EnumType.STRING)
    private BinModificationStatus status;

    private String modificationReason;

    @Builder
    public BinModification(Member member, Bin bin, String title, String address, BinType type, String imageUrl,
                           double latitude, double longitude, BinModificationStatus status, String modificationReason) {
        this.member = member;
        this.bin = bin;
        this.title = title;
        this.address = address;
        this.type = type;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.modificationReason = modificationReason;
    }

    public boolean isPending() {
        return this.status == BinModificationStatus.PENDING;
    }

    public void approve() {
        this.status = BinModificationStatus.APPROVED;
    }

    public void reject() {
        this.status = BinModificationStatus.REJECTED;
    }
}
