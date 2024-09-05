package net.binder.api.bin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinUpdate {

    private final String title;

    private final String address;

    private final String type;

    private final String imageUrl;

    private final double latitude;

    private final double longitude;
}
