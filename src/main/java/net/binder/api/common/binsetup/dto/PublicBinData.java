package net.binder.api.common.binsetup.dto;

import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.BinType;

@Getter
public class PublicBinData {

    private String title;

    private String address;

    private BinType type;

    private String imageUrl;

    @Builder
    public PublicBinData(String title, String address, BinType type, String imageUrl) {
        this.title = title;
        this.address = address;
        this.type = type;
        this.imageUrl = imageUrl;
    }
}
