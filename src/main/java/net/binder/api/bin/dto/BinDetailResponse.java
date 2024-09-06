package net.binder.api.bin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.dto.BaseResponse;

@Getter
public class BinDetailResponse extends BaseResponse {

    private final String title;

    private final BinType type;

    private final Double latitude;

    private final Double longitude;

    private final String address;

    private final Long likeCount;

    private final Long dislikeCount;

    private final String imageUrl;

    @Builder
    public BinDetailResponse(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt, String title, BinType type,
                             Double latitude, Double longitude, String address, Long likeCount, Long dislikeCount,
                             String imageUrl) {
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

    public static BinDetailResponse from(Bin bin) {
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
