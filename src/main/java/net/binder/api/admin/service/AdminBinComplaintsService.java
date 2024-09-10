package net.binder.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.repository.AdminBinComplaintQueryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminBinComplaintsService {

    private final AdminBinComplaintQueryRepository adminBinComplaintRepository;

    public List<BinComplaintDetail> getBinComplaintDetails(ComplaintFilter filter) {

        return null;
    }
}
