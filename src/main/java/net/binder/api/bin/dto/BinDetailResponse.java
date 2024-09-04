package net.binder.api.bin.dto;

import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.dto.BaseResponse;

import java.time.LocalDateTime;

@Getter
public class BinDetailResponse extends BaseResponse {

    private String title;

    private BinType type;

    private double latitude;

    private double longitude;

    private String address;

    private long matchCount;

    private long mismatchCount;

    private String imageUrl;

    private LocalDateTime deletedAt;

    @Builder
    public BinDetailResponse(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt, String title, BinType type, double latitude, double longitude, String address, long matchCount, long mismatchCount, String imageUrl, LocalDateTime deletedAt) {
        super(id, createdAt, modifiedAt);
        this.title = title;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.matchCount = matchCount;
        this.mismatchCount = mismatchCount;
        this.imageUrl = imageUrl;
        this.deletedAt = deletedAt;
    }

    public static BinDetailResponse from(Bin bin){
        return BinDetailResponse.builder()
                .id(bin.getId())
                .title(bin.getTitle())
                .latitude(bin.getPoint().getCoordinate().getX())
                .longitude(bin.getPoint().getCoordinate().getY())
                .type(bin.getType())
                .address(bin.getAddress())
                .matchCount(bin.getMatchCount())
                .mismatchCount(bin.getMismatchCount())
                .imageUrl(bin.getImageUrl())
                .createdAt(bin.getCreatedAt())
                .modifiedAt(bin.getModifiedAt())
                .build();
    }

}
