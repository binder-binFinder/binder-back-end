package net.binder.api.bin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinUpdateRequest {

    @NotBlank
    private final String title;

    @NotBlank
    private final String address;

    @NotBlank
    private final String type;

    @NotBlank
    private final String imageUrl;

    @NotBlank
    private final Double latitude;

    @NotBlank
    private final Double longitude;
}
