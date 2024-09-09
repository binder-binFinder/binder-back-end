package net.binder.api.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinRegistrationListResponse {

    private final List<BinRegistrationDetail> binRegistrationDetails;

    private final Long pendingCount;
}
