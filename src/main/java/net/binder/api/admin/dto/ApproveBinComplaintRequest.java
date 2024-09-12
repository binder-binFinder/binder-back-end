package net.binder.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ApproveBinComplaintRequest {

    @NotBlank
    private final String approveReason;

    @JsonCreator
    public ApproveBinComplaintRequest(String approveReason) {
        this.approveReason = approveReason;
    }
}
