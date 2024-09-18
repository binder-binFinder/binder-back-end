package net.binder.api.notification.entity;

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
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.entity.BaseEntityWithSoftDelete;
import net.binder.api.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntityWithSoftDelete {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean isRead;

    private String additionalInfo;

    @Builder
    public Notification(Member sender, Member receiver, Bin bin, NotificationType type, String additionalInfo) {
        this.sender = sender;
        this.receiver = receiver;
        this.bin = bin;
        this.type = type;
        this.isRead = false;
        this.additionalInfo = additionalInfo;

    }

    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isOwner(Member member) {
        return this.receiver.equals(member);
    }
}