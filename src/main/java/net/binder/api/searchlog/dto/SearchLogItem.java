package net.binder.api.searchlog.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SearchLogItem {

    private final Long id;

    private final String keyword;

    private final String address;

    private final boolean hasBinsNearby;

    private final LocalDateTime createdAt;

}
