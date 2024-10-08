package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.ApproveBinComplaintRequest;
import net.binder.api.admin.dto.BinComplaintCountsPerTypeResponse;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.BinComplaintListResponse;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.dto.RejectBinComplaintRequest;
import net.binder.api.admin.dto.TypeCount;
import net.binder.api.admin.service.AdminBinComplaintsService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins/complaints")
@Tag(name = "관리자용 쓰레기통 신고 심사 관리")
public class AdminBinComplaintController {

    private final AdminBinComplaintsService adminBinComplaintsService;

    @Operation(summary = "쓰레기통 신고 심사 목록 조회")
    @GetMapping
    public BinComplaintListResponse getBinComplaints(@RequestParam(defaultValue = "ENTIRE") ComplaintFilter filter) {
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(filter);
        Long pendingCount = adminBinComplaintsService.getComplaintPendingCount();
        return new BinComplaintListResponse(binComplaintDetails, pendingCount);
    }

    @Operation(summary = "쓰레기통 신고 타입별 세부 카운트 조회")
    @GetMapping("/{id}/counts")
    public BinComplaintCountsPerTypeResponse getBinComplaintCountsPerType(@PathVariable Long id) {
        List<TypeCount> typeCounts = adminBinComplaintsService.getBinComplaintCountsPerType(id);
        return new BinComplaintCountsPerTypeResponse(typeCounts);
    }

    @Operation(summary = "쓰레기통 신고 승인")
    @PostMapping("/{id}/approve")
    public void approveBinComplaint(@CurrentUser String email, @PathVariable Long id,
                                    @Valid @RequestBody ApproveBinComplaintRequest request) {
        adminBinComplaintsService.approve(email, id, request.getApproveReason());
    }

    @Operation(summary = "쓰레기통 신고 거절")
    @PostMapping("/{id}/reject")
    public void rejectBinComplaint(@CurrentUser String email, @PathVariable Long id,
                                   @Valid @RequestBody RejectBinComplaintRequest request) {
        adminBinComplaintsService.reject(email, id, request.getRejectReason());
    }
}
