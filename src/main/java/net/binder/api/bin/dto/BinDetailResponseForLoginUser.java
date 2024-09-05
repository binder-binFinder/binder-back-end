package net.binder.api.bin.dto;

import lombok.Getter;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.dto.BaseResponse;

import java.time.LocalDateTime;

@Getter
public class BinDetailResponseForLoginUser extends BaseResponse {

    private String title;

    private BinType type;

    private double latitude;

    private double longitude;

    private String address;

    private long likeCount;

    private long dislikeCount;

    private String imageUrl;

    private boolean isLiked;

    private boolean isDisliked;

    private boolean isBookMarked;

    public BinDetailResponseForLoginUser(Long id, LocalDateTime createdAt, LocalDateTime modifiedAt,
                                         String title, BinType type,
                                         Double latitude, Double longitude, String address,
                                         Long likeCount, Long dislikeCount, String imageUrl,
                                         Boolean isLiked, Boolean isDisliked, Boolean isBookMarked) {
        super(id, createdAt, modifiedAt);
        this.title = title;
        this.type = type;
        this.latitude = latitude != null ? latitude : 0;
        this.longitude = longitude != null ? longitude : 0;
        this.address = address;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.dislikeCount = dislikeCount != null ? dislikeCount : 0;
        this.imageUrl = imageUrl;
        this.isLiked = isLiked != null ? isLiked : false;
        this.isDisliked = isDisliked != null ? isDisliked : false;
        this.isBookMarked = isBookMarked != null ? isBookMarked : false;
    }
}
