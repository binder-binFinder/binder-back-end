package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.BinComplaintListResponse;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.service.AdminBinComplaintsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins/complaints")
@Tag(name = "관리자용 쓰레기통 신고 심사 관리")
public class AdminBinComplaintController {

    private final AdminBinComplaintsService adminBinComplaintsService;

    @GetMapping
    public BinComplaintListResponse getBinComplaints(@RequestParam(defaultValue = "ENTIRE") ComplaintFilter filter) {
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(filter);
        Long pendingCount = adminBinComplaintsService.getComplaintPendingCount();
        return new BinComplaintListResponse(binComplaintDetails, pendingCount);
    }

}
