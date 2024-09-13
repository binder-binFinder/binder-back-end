package net.binder.api.search.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;

@Getter
@RequiredArgsConstructor
public class SearchDto {

    private final BinType type;

    @NotNull
    private final Double latitude;

    @NotNull
    private final Double longitude;

    @NotNull
    private final Integer radius;

}
