package net.binder.api.bookmark.dto;

import lombok.*;
import net.binder.api.bin.entity.BinType;


@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class BookmarkResponse {

    private Long bookmarkId;

    private Long binId;

    private String address;

    private String title;

    private BinType binType;

    private Double distance;

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
