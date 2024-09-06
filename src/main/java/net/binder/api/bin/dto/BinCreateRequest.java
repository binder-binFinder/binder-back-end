package net.binder.api.bin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinCreateRequest {

    @NotBlank
    private final String title;

    @NotBlank
    private final String address;

    @NotBlank
    private final String type;

    @NotBlank
    private final String imageUrl;

    @NotNull
    private final Double latitude;

    @NotNull
    private final Double longitude;
}
