package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.AdminBinUpdateRequest;
import net.binder.api.admin.dto.DeleteBinRequest;
import net.binder.api.admin.service.AdminBinManagementService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins")
@Tag(name = "관리자용 쓰레기통 관리")
public class AdminBinManagementController {

    private final AdminBinManagementService adminBinManagementService;

    @Operation(summary = "쓰레기통 즉시 수정")
    @PatchMapping("/{id}")
    public void updateBin(@CurrentUser String email, @PathVariable Long id,
                          @Valid @RequestBody AdminBinUpdateRequest request) {
        adminBinManagementService.updateBin(email, id, request);
    }

    @Operation(summary = "쓰레기통 즉시 삭제")
    @DeleteMapping("/{id}")
    public void deleteBin(@CurrentUser String email, @PathVariable Long id,
                          @Valid @RequestBody DeleteBinRequest request) {
        adminBinManagementService.deleteBin(email, id, request.getDeleteReason());
    }
}
