package net.binder.api.complaint.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;
import net.binder.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComplaintInfo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    private ComplaintType type;

    public ComplaintInfo(Complaint complaint, Member member, ComplaintType type) {
        this.complaint = complaint;
        this.member = member;
        this.type = type;
    }
}
