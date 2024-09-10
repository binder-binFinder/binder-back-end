package net.binder.api.complaint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.complaint.entity.ComplaintType;

@Getter
@RequiredArgsConstructor
public class CreateComplaintRequest {

    @NotNull
    private final Long binId;

    @NotNull
    private final ComplaintType complaintType;
}
