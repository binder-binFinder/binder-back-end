package net.binder.api.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BinComplaintCountsPerTypeResponse {

    private final List<TypeCount> counts;
}
