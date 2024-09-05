package net.binder.api.binmodification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.entity.BaseEntity;
import net.binder.api.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BinModification extends BaseEntity {

    private String title;

    private String address;

    private BinType type;

    private String imageUrl;

    private double latitude;

    private double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String rejectionReason;

    @Builder
    public BinModification(String title, String address, BinType type, String imageUrl, double latitude, double longitude, Member member, Status status, String rejectionReason) {
        this.title = title;
        this.address = address;
        this.type = type;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.member = member;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }
}
