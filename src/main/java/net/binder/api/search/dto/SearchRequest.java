package net.binder.api.search.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;

@Getter
@RequiredArgsConstructor
public class SearchRequest {

    private final BinType type;

    private final Double latitude;

    private final Double longitude;

    private final Integer radius;

}
