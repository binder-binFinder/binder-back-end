package net.binder.api.memberlikebin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.entity.IdEntity;
import net.binder.api.member.entity.Member;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
public class MemberLikeBin extends IdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @CreatedDate
    private LocalDateTime createdAt;
}
