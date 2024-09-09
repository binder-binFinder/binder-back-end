package net.binder.api.bin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;

@Getter
@RequiredArgsConstructor
public class BinCreateRequest {

    @NotBlank
    private final String title;

    @NotBlank
    private final String address;

    @NotNull
    private final BinType type;

    @NotBlank
    private final String imageUrl;

    @NotNull
    private final Double latitude;

    @NotNull
    private final Double longitude;
}
