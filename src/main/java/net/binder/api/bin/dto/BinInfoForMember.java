package net.binder.api.bin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinInfoForMember {

    private final Boolean isOwner;

    private final Boolean isLiked;

    private final Boolean isDisliked;

    private final Boolean isBookMarked;
}
