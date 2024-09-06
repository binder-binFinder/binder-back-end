package net.binder.api.memberdislikebin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.memberdislikebin.service.MemberDislikeBinService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
public class MemberDislikeBinController {

    private final MemberDislikeBinService memberDislikeBinService;

    @Operation(summary = "쓰레기통 싫어요")
    @PatchMapping("/dislikes/{id}")
    public void saveLike(@CurrentUser String email, @PathVariable("id") Long id) {
        memberDislikeBinService.saveDislike(email, id);
    }

    @Operation(summary = "쓰레기통 싫어요 취소")
    @DeleteMapping("/dislikes/{id}")
    public void deleteLike(@CurrentUser String email, @PathVariable("id") Long id) {
        memberDislikeBinService.deleteDisLike(email, id);
    }
}
