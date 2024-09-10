package net.binder.api.complaint.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Complaint extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    private Long count;

    public Complaint(Bin bin, ComplaintStatus status, Long count) {
        this.bin = bin;
        this.status = status;
        this.count = count;
    }

    public void increaseCount() {
        count++;
    }

    public void approve() {
        this.status = ComplaintStatus.APPROVED;
        bin.softDelete();
    }

    public void reject() {
        this.status = ComplaintStatus.REJECTED;
    }
}
