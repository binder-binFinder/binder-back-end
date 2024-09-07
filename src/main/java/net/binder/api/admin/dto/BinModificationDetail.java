package net.binder.api.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.binmodification.entity.BinModification;
import net.binder.api.binmodification.entity.BinModificationStatus;

@Getter
@RequiredArgsConstructor
@Builder
public class BinModificationDetail {

    private final Long modificationId;

    private final Long binId;

    private final String title;

    private final String address;

    private final Double latitude;

    private final Double longitude;

    private final String nickname;

    private final BinType type;

    private final BinModificationStatus status;

    private final String image_url;

    private final LocalDateTime createdAt;


    public static BinModificationDetail from(BinModification binModification) {
        return BinModificationDetail.builder()
                .modificationId(binModification.getId())
                .binId(binModification.getBin().getId())
                .title(binModification.getTitle())
                .address(binModification.getAddress())
                .latitude(binModification.getLatitude())
                .longitude(binModification.getLongitude())
                .nickname(binModification.getMember().getNickname())
                .type(binModification.getType())
                .status(binModification.getStatus())
                .image_url(binModification.getImageUrl())
                .createdAt(binModification.getCreatedAt())
                .build();
    }
}
