package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinModificationDetail;
import net.binder.api.admin.dto.BinModificationListResponse;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.admin.service.AdminBinModificationService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins/modifications")
@Tag(name = "관리자용 쓰레기통 수정 심사 관리")
public class AdminBinModificationController {

    private final AdminBinModificationService adminBinModificationService;

    @Operation(summary = "쓰레기통 수정 심사 목록")
    @GetMapping
    public BinModificationListResponse getBinModifications(
            @RequestParam(defaultValue = "ENTIRE") ModificationFilter filter) {
        List<BinModificationDetail> binModificationDetails = adminBinModificationService.getBinModificationDetails(
                filter);
        Long pendingCount = adminBinModificationService.getModificationPendingCount();
        return new BinModificationListResponse(binModificationDetails, pendingCount);
    }

    @Operation(summary = "쓰레기통 수정 요청 승인")
    @PostMapping("/{id}/approve")
    public void approveModification(@CurrentUser String email, @PathVariable Long id) {
        adminBinModificationService.approveModification(email, id);
    }
}
