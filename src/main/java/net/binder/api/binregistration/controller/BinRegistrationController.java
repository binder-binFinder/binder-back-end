package net.binder.api.binregistration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.binregistration.dto.BinCreateRequest;
import net.binder.api.binregistration.service.BinRegistrationService;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.member.entity.Member;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
@Tag(name = "쓰레기통 등록 요청")
public class BinRegistrationController {

    private final BinRegistrationService binRegistrationService;

    @Operation(summary = "쓰레기통 생성 요청")
    @PostMapping("/create")
    public void save(@CurrentUser String email, @RequestBody BinCreateRequest binCreateRequest){
        binRegistrationService.create(binCreateRequest, email);
    }
}
