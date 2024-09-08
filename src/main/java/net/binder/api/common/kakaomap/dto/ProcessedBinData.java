package net.binder.api.common.kakaomap.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.binsetup.dto.PublicBinData;

@Getter
@RequiredArgsConstructor
@Builder
public class ProcessedBinData {

    private final String title;

    private final String address;

    private final BinType type;

    private final String imageUrl;

    private final Double longitude;

    private final Double latitude;

    public static ProcessedBinData from(PublicBinData data, Double longitude, Double latitude) {
        return ProcessedBinData.builder()
                .title(data.getTitle())
                .address(data.getAddress())
                .type(data.getType())
                .imageUrl(null)
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }
}
