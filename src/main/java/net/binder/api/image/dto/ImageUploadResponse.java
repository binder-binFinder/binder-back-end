package net.binder.api.image.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ImageUploadResponse {

    private final String imageUrl;
}
