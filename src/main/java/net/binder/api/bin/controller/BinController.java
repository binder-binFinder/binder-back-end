package net.binder.api.bin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.dto.BinCreateRequest;
import net.binder.api.bin.dto.BinDetailResponse;
import net.binder.api.bin.dto.BinUpdateRequest;
import net.binder.api.bin.service.BinService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
@Tag(name = "쓰레기통 관리")
public class BinController {

    private final BinService binService;

    @Operation(summary = "쓰레기통 생성 요청")
    @PostMapping
    public void requestBinRegistration(@CurrentUser String email,
                                       @Valid @RequestBody BinCreateRequest binCreateRequest) {
        binService.requestBinRegistration(binCreateRequest, email);
    }

    @Operation(summary = "쓰레기통 상세 조회")
    @GetMapping("/{id}")
    public BinDetailResponse getBins(@CurrentUser String email, @PathVariable("id") Long id) {
        return binService.getBinDetail(email, id);
    }

    @Operation(summary = "쓰레기통 수정 요청")
    @PatchMapping("/{id}")
    public void requestBinModification(@CurrentUser String email, @PathVariable("id") Long id,
                                       @Valid @RequestBody BinUpdateRequest binUpdateRequest) {
        binService.requestBinModification(email, id, binUpdateRequest);
    }
}
