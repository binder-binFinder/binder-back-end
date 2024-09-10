package net.binder.api.complaint.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.entity.BaseEntityWithSoftDelete;
import net.binder.api.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Complaint extends BaseEntityWithSoftDelete {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @Enumerated(EnumType.STRING)
    private ComplaintType type;

    public Complaint(Member member, Bin bin, ComplaintType type) {
        this.member = member;
        this.bin = bin;
        this.type = type;
    }
}
