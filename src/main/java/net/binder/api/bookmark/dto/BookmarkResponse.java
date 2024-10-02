package net.binder.api.bookmark.dto;

import lombok.*;
import net.binder.api.bin.entity.BinType;


@Builder
@Getter
@RequiredArgsConstructor
public class BookmarkResponse {

    private final Long bookmarkId;

    private final Long binId;

    private final String address;

    private final String title;

    private final BinType binType;

    private final Double longitude;

    private final Double latitude;

    private final Double distance;

}
