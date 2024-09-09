package net.binder.api.bin.entity;

import java.time.LocalDateTime;

public interface BinDetailProjection {

    Long getId();

    LocalDateTime getCreatedAt();

    LocalDateTime getModifiedAt();

    String getTitle();

    BinType getType();

    Double getLatitude();

    Double getLongitude();

    String getAddress();

    Long getLikeCount();

    Long getDislikeCount();

    Long getBookmarkCount();

    String getImageUrl();

    Integer getIsOwner();

    Integer getIsLiked();

    Integer getIsDisliked();

    Integer getIsBookmarked();
}
