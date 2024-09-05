package net.binder.api.memberdislikebin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.memberdislikebin.service.MemberDislikeBinService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
public class MemberDislikeBinController {

    private final MemberDislikeBinService memberDislikeBinService;

    @Operation(summary = "쓰레기통 싫어요")
    @PatchMapping("/dislikes/{id}")
    public void saveLike(@CurrentUser String email, @PathVariable("id") long id) {
        memberDislikeBinService.saveDislike(email, id);
    }

    @Operation(summary = "쓰레기통 싫어요 취소")
    @DeleteMapping("/dislikes/{id}")
    public void deleteLike(@CurrentUser String email, @PathVariable("id") long id) {
        memberDislikeBinService.deleteDisLike(email, id);
    }
}
