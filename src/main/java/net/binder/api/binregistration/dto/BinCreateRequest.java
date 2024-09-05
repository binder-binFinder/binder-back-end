package net.binder.api.binregistration.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinCreateRequest {

    private final String title;

    private final String address;

    private final String type;

    private final String imageUrl;

    private final double latitude;

    private final double longitude;

}
