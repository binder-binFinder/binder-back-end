package net.binder.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RejectBinModificationRequest {

    @NotBlank
    private final String rejectReason;

    @JsonCreator
    public RejectBinModificationRequest(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
