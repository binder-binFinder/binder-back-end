package net.binder.api.bookmark.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBookmarkRequest {

    @NotNull
    private Long binId;

}