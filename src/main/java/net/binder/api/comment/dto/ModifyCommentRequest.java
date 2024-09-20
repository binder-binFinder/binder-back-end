package net.binder.api.comment.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ModifyCommentRequest {

    @JsonCreator
    public ModifyCommentRequest(String content) {
        this.content = content;
    }

    @NotBlank
    private final String content;
}
