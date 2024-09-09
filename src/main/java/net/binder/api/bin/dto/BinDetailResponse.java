package net.binder.api.bin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinDetailProjection;
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

    private final Long bookmarkCount;

    private final String imageUrl;

    private final BinInfoForMember binInfoForMember;

    @Builder
    public BinDetailResponse(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt, String title, BinType type,
                             Double latitude, Double longitude, String address, Long likeCount, Long dislikeCount,
                             Long bookmarkCount, String imageUrl, BinInfoForMember binInfoForMember) {
        super(id, createdAt, modifiedAt);
        this.title = title;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.bookmarkCount = bookmarkCount;
        this.imageUrl = imageUrl;
        this.binInfoForMember = binInfoForMember;
    }

    public static BinDetailResponse from(Bin bin) {
        return BinDetailResponse.builder()
                .id(bin.getId())
                .createdAt(bin.getCreatedAt())
                .modifiedAt(bin.getModifiedAt())
                .title(bin.getTitle())
                .type(bin.getType())
                .latitude(bin.getPoint().getY())
                .longitude(bin.getPoint().getX())
                .address(bin.getAddress())
                .likeCount(bin.getLikeCount())
                .dislikeCount(bin.getDislikeCount())
                .bookmarkCount(bin.getBookmarkCount())
                .imageUrl(bin.getImageUrl())
                .build();
    }

    public static BinDetailResponse from(BinDetailProjection bin) {

        BinInfoForMember binInfoForMember = new BinInfoForMember(bin.getIsOwner() == 1, bin.getIsLiked() == 1,
                bin.getIsDisliked() == 1, bin.getIsBookmarked() == 1);

        return BinDetailResponse.builder()
                .id(bin.getId())
                .createdAt(bin.getCreatedAt())
                .modifiedAt(bin.getModifiedAt())
                .title(bin.getTitle())
                .type(bin.getType())
                .latitude(bin.getLatitude())
                .longitude(bin.getLongitude())
                .address(bin.getAddress())
                .likeCount(bin.getLikeCount())
                .dislikeCount(bin.getDislikeCount())
                .bookmarkCount(bin.getBookmarkCount())
                .imageUrl(bin.getImageUrl())
                .binInfoForMember(binInfoForMember)
                .build();
    }
}
