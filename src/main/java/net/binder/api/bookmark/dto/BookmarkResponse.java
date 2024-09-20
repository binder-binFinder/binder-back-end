package net.binder.api.bookmark.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;

@RequiredArgsConstructor
@Builder
@Getter
public class BookmarkResponse {

    private final Long binId;

    private final String address;

    private final String title;

    private final BinType binType;

    private final Double distance;

    public static BookmarkResponse from(BookmarkProjection projection) {
        return BookmarkResponse.builder()
                .binId(projection.getBinId())
                .address(projection.getAddress())
                .title(projection.getTitle())
                .binType(projection.getBinType())
                .distance(projection.getDistance())
                .build();
    }
}
