package net.binder.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RejectBinRegistrationRequest {

    @NotBlank
    private final String rejectReason;

    @JsonCreator
    public RejectBinRegistrationRequest(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
