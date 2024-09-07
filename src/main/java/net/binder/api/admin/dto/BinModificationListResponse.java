package net.binder.api.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BinModificationListResponse {

    private final List<BinModificationDetail> binModificationDetails;

    private final Long pendingCount;
}
