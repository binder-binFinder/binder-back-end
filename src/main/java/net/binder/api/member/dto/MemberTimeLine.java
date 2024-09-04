package net.binder.api.member.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.binder.api.bin.entity.BinType;
import net.binder.api.membercreatebin.entity.MemberCreateBinStatus;

@Getter
@ToString
public class MemberTimeLine {

    private final Long id;

    private final String title;

    private final String address;

    private final BinType type;

    private final MemberCreateBinStatus status;

    private final LocalDateTime createdAt;

    private final Long bookmarkCount;

    @Builder
    public MemberTimeLine(Long id, String title, String address, BinType type, MemberCreateBinStatus status,
                          LocalDateTime createdAt,
                          Long bookmarkCount) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.bookmarkCount = bookmarkCount;
    }
}
