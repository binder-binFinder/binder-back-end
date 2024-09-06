package net.binder.api.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.service.AdminBinService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/bins")
@Tag(name = "관리자용 쓰레기통 관리")
public class AdminBinController {

    private final AdminBinService adminBinService;

    @PostMapping("{id}/approve")
    public void approveBinRegistration(@PathVariable Long id) {

        adminBinService.approveRegistration(id);
    }
}
