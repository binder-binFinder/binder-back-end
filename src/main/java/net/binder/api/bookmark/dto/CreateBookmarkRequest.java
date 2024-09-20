package net.binder.api.bookmark.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
public class CreateBookmarkRequest {

    @NotNull
    private Long binId;

}
