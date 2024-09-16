package net.binder.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DeleteBinRequest {

    @NotBlank
    private final String deleteReason;

    public DeleteBinRequest(String deleteReason) {
        this.deleteReason = deleteReason;
    }
}
