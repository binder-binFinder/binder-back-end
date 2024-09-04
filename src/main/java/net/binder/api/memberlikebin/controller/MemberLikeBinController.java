package net.binder.api.memberlikebin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.memberlikebin.service.MemberLikeBinService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bins/likes")
@Tag(name = "쓰레기통 좋아요")
public class MemberLikeBinController {

    private final MemberLikeBinService memberLikeBinService;
}
