package net.binder.api.bin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.bin.dto.BinDetailResponse;
import net.binder.api.bin.dto.BinSave;
import net.binder.api.bin.dto.BinUpdate;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
@Tag(name = "쓰레기통 관리")
public class BinController {

    private final BinService binService;

    @Operation(summary = "쓰레기통 생성")
    @PostMapping("/save")
    public void save(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @RequestBody BinSave binSave){
        binService.save(binSave, customOAuth2User);
    }

    @Operation(summary = "쓰레기통 조회")
    @GetMapping("/{id}")
    public BinDetailResponse getBins(@PathVariable("id") Long id){
        Bin bin = binService.findById(id);

        return BinDetailResponse.from(bin);
    }

    @Operation(summary = "쓰레기통 삭제")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id){
        binService.delete(id);
    }

    @Operation(summary = "쓰레기통 업데이트")
    @PatchMapping("/{id}")
    public void update(@PathVariable("id") Long id, @RequestBody BinUpdate binUpdate){
        binService.update(id, binUpdate);
    }

    @Operation(summary = "쓰레기통 위치 정확해요 버튼")
    @PatchMapping("/{id}")
    public void matchUp(){

    }
}
