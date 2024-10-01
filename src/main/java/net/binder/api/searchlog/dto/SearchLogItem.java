package net.binder.api.searchlog.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SearchLogItem {

    private final Long id;

    private final String keyword;

    private final String address;

    private final boolean hasBookmarkedBin;

    private final boolean hasBinsNearby;

}
