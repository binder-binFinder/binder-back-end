package net.binder.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;

@Getter
@RequiredArgsConstructor
public class AdminBinUpdateRequest {

    @NotBlank
    private final String title;

    @NotBlank
    private final String address;

    @NotNull
    private final BinType type;

    private final String imageUrl;

    @NotNull
    private final Double latitude;

    @NotNull
    private final Double longitude;

    @NotBlank
    private final String modificationReason;

    private final Long registrationId;

    private final Long modificationId;
}
