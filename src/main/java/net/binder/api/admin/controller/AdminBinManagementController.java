package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.service.AdminBinManagementService;
import net.binder.api.bin.dto.BinUpdateRequest;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins")
@Tag(name = "관리자용 쓰레기통 관리")
public class AdminBinManagementController {

    private final AdminBinManagementService adminBinManagementService;

    @PatchMapping("/{id}")
    public void updateBin(@CurrentUser String email, @PathVariable Long id, BinUpdateRequest request) {
        adminBinManagementService.updateBin(email, id, request);
    }
}
