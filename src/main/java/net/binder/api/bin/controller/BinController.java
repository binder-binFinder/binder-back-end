package net.binder.api.bin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.dto.BinCreateRequest;
import net.binder.api.bin.dto.BinDetailResponse;
import net.binder.api.bin.dto.BinDetailResponseForLoginUser;
import net.binder.api.bin.dto.BinUpdateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.member.entity.Member;
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
    public void requestBinRegistration(@CurrentUser String email, @RequestBody BinCreateRequest binCreateRequest) {
        binService.requestBinRegistration(binCreateRequest, email);
    }

    @Operation(summary = "로그인 유저 쓰레기통 조회")
    @GetMapping("/login/{id}")
    public BinDetailResponseForLoginUser getBinsForLoginUser(@CurrentUser Member member, @PathVariable("id") Long id) {
        return binService.findByIdForLoginUser(member, id);
    }

    @Operation(summary = "비로그인 유저 쓰레기통 조회")
    @GetMapping("/{id}")
    public BinDetailResponse getBins(@PathVariable("id") Long id) {
        Bin bin = binService.findById(id);
        return BinDetailResponse.from(bin);
    }

    @Operation(summary = "쓰레기통 수정 요청")
    @PatchMapping("/{id}")
    public void requestBinModification(@CurrentUser String email, @PathVariable("id") Long id,
                                       @RequestBody BinUpdateRequest binUpdateRequest) {
        binService.requestBinModification(email, id, binUpdateRequest);
    }
}
