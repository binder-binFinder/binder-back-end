package net.binder.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DeleteBinRequest {

    @NotBlank
    private final String deleteReason;

    @JsonCreator
    public DeleteBinRequest(String deleteReason) {
        this.deleteReason = deleteReason;
    }
}
