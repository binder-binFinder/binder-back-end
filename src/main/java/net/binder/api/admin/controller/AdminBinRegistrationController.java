package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinRegistrationDetail;
import net.binder.api.admin.dto.BinRegistrationListResponse;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.admin.dto.RejectBinRegistrationRequest;
import net.binder.api.admin.service.AdminBinRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins")
@Tag(name = "관리자용 쓰레기 등록 심사 관리")
public class AdminBinRegistrationController {

    private final AdminBinRegistrationService adminBinRegistrationService;

    @Operation(summary = "쓰레기통 등록 승인")
    @PostMapping("/registrations/{id}/approve")
    public void approveBinRegistration(@PathVariable Long id) {

        adminBinRegistrationService.approveRegistration(id);
    }

    @Operation(summary = "쓰레기통 등록 거절")
    @PostMapping("/registrations/{id}/reject")
    public void rejectBinRegistration(@PathVariable Long id, @Valid @RequestBody RejectBinRegistrationRequest request) {
        adminBinRegistrationService.rejectRegistration(id, request.getRejectReason());
    }

    @Operation(summary = "쓰레기통 등록 심사 목록")
    @GetMapping("/registrations")
    public BinRegistrationListResponse getBinRegistrations(
            @RequestParam(defaultValue = "ENTIRE") RegistrationFilter filter) {
        List<BinRegistrationDetail> binRegistrationDetails = adminBinRegistrationService.getBinRegistrationDetails(
                filter);
        Long pendingCount = adminBinRegistrationService.getRegistrationPendingCount();
        return new BinRegistrationListResponse(binRegistrationDetails, pendingCount);
    }
}
