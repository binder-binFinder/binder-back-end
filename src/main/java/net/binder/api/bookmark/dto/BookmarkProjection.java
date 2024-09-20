package net.binder.api.bookmark.dto;

import net.binder.api.bin.entity.BinType;

public interface BookmarkProjection {

    Long getBinId();

    String  getAddress();

    String getTitle();

    BinType getBinType();

    Double getDistance();
}
