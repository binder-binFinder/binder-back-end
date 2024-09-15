package net.binder.api.search.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;

@Getter
@RequiredArgsConstructor
public class SearchDto {

    private final BinType type;

    private final Double latitude;

    private final Double longitude;

    private final Integer radius;

}
