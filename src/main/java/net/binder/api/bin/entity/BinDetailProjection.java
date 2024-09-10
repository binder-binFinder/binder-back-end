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

    Boolean getIsOwner();

    Boolean getIsLiked();

    Boolean getIsDisliked();

    Boolean getIsBookmarked();
}
