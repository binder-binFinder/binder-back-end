package net.binder.api.bin.entity;

import static net.binder.api.bin.entity.BinRegistrationStatus.APPROVED;
import static net.binder.api.bin.entity.BinRegistrationStatus.PENDING;
import static net.binder.api.bin.entity.BinRegistrationStatus.REJECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;
import net.binder.api.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BinRegistration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @Enumerated(EnumType.STRING)
    private BinRegistrationStatus status;

    @Builder
    public BinRegistration(Member member, Bin bin, BinRegistrationStatus status) {
        this.member = member;
        this.bin = bin;
        this.status = status;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    public void approve() {
        this.status = APPROVED;
    }

    public void reject() {
        this.status = REJECTED;
        this.bin.softDelete();
    }

    public boolean isPending() {
        return this.status == PENDING;
    }
}
