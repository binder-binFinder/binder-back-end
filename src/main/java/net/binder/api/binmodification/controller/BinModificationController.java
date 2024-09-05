package net.binder.api.binmodification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.binmodification.dto.BinUpdateRequest;
import net.binder.api.binmodification.service.BinModificationService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
@Tag(name = "쓰레기통 수정 요청")
public class BinModificationController {

    private final BinModificationService binModificationService;

    @Operation(summary = "쓰레기통 수정 요청")
    @PostMapping("/update")
    public void save(@CurrentUser String email, @RequestBody BinUpdateRequest binUpdateRequest){
        binModificationService.create(binUpdateRequest, email);
    }
}
