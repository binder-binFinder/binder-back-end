package net.binder.api.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;
import net.binder.api.bin.entity.BinType;

@Getter
@RequiredArgsConstructor
@Builder
public class BinRegistrationDetail {

    private final Long registrationId;

    private final Long binId;

    private final String title;

    private final String address;

    private final Double longitude;

    private final Double latitude;

    private final String nickname;

    private final BinType type;

    private final BinRegistrationStatus status;

    private final String imageUrl;

    private final LocalDateTime createdAt;


    public static BinRegistrationDetail from(BinRegistration binRegistration) {
        Bin bin = binRegistration.getBin();

        return BinRegistrationDetail.builder()
                .registrationId(binRegistration.getId())
                .binId(bin.getId())
                .title(bin.getTitle())
                .address(bin.getAddress())
                .longitude(bin.getPoint().getX())
                .latitude(bin.getPoint().getY())
                .nickname(binRegistration.getMember().getNickname())
                .type(bin.getType())
                .status(binRegistration.getStatus())
                .imageUrl(bin.getImageUrl())
                .createdAt(binRegistration.getCreatedAt())
                .build();
    }
}
