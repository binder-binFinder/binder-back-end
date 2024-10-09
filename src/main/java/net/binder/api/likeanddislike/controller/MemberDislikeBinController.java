package net.binder.api.likeanddislike.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.likeanddislike.service.LikeAndDislikeFacade;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins")
@Tag(name = "쓰레기통 싫어요 관리")
public class MemberDislikeBinController {

    private final LikeAndDislikeFacade likeAndDislikeFacade;

    @Operation(summary = "쓰레기통 싫어요")
    @PatchMapping("/dislikes/{id}")
    public void saveLike(@CurrentUser String email, @PathVariable("id") Long id) {
        likeAndDislikeFacade.createDislike(email, id);
    }

    @Operation(summary = "쓰레기통 싫어요 취소")
    @DeleteMapping("/dislikes/{id}")
    public void deleteLike(@CurrentUser String email, @PathVariable("id") Long id) {
        likeAndDislikeFacade.deleteDislike(email, id);
    }
}
