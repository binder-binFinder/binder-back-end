package net.binder.api.member.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;

@Getter
public class BinRegistrationActivity {

    private final Long binId;

    private final String title;

    private final String address;

    private final BinType type;

    private final BinRegistrationStatus status;

    private final LocalDateTime createdAt;

    private final LocalDateTime modifiedAt;

    private final Long bookmarkCount;

    private final Boolean isDeleted;

    @Builder
    public BinRegistrationActivity(Long binId, String title, String address, BinType type, BinRegistrationStatus status,
                                   LocalDateTime createdAt,
                                   LocalDateTime modifiedAt,
                                   Long bookmarkCount, Boolean isDeleted) {
        this.binId = binId;
        this.title = title;
        this.address = address;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.bookmarkCount = bookmarkCount;
        this.isDeleted = isDeleted;
    }

    public static BinRegistrationActivity from(BinRegistration binRegistration) {
        Bin bin = binRegistration.getBin();
        return BinRegistrationActivity.builder()
                .binId(bin.getId())
                .title(bin.getTitle())
                .address(bin.getAddress())
                .type(bin.getType())
                .status(binRegistration.getStatus())
                .createdAt(bin.getCreatedAt())
                .modifiedAt(bin.getModifiedAt())
                .bookmarkCount(bin.getBookmarkCount())
                .isDeleted(bin.isDeleted())
                .build();
    }
}
