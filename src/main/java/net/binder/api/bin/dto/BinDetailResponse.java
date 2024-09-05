package net.binder.api.bin.dto;

import lombok.Builder;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.dto.BaseResponse;

import java.time.LocalDateTime;

public class BinDetailResponse extends BaseResponse {

    private String title;

    private BinType type;

    private double latitude;

    private double longitude;

    private String address;

    private long likeCount;

    private long dislikeCount;

    private String imageUrl;

    @Builder
    public BinDetailResponse(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt, String title, BinType type, double latitude, double longitude, String address, long likeCount, long dislikeCount, String imageUrl) {
        super(id, createdAt, modifiedAt);
        this.title = title;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.imageUrl = imageUrl;
    }

    public static BinDetailResponse from(Bin bin){
        return BinDetailResponse.builder()
                .id(bin.getId())
                .createdAt(bin.getCreatedAt())
                .modifiedAt(bin.getModifiedAt())
                .title(bin.getTitle())
                .type(bin.getType())
                .latitude(bin.getPoint().getX())
                .longitude(bin.getPoint().getY())
                .address(bin.getAddress())
                .likeCount(bin.getLikeCount())
                .dislikeCount(bin.getDislikeCount())
                .imageUrl(bin.getImageUrl())
                .build();
    }
}
