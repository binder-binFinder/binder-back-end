package net.binder.api.admin.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.complaint.entity.ComplaintStatus;

@RequiredArgsConstructor
@Getter
public class BinComplaintDetail {

    private final Long complaintId;

    private final Long binId;

    private final String title;

    private final String address;

    private final BinType type;

    private final ComplaintStatus status;

    private final String imageUrl;

    private final LocalDateTime mostRecentComplaintAt;

    private final Long complaintCount;
}
