package net.binder.api.bookmark.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class CreateBookmarkRequest {

    @NotNull
    private Long binId;

}