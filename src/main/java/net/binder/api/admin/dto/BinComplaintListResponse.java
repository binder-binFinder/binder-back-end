package net.binder.api.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinComplaintListResponse {

    private final List<BinComplaintDetail> binComplaintDetails;

    private final Long pendingCount;
}
