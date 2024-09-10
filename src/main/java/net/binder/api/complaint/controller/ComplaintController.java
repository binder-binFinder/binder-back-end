package net.binder.api.complaint.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.complaint.dto.CreateComplaintRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/complaints")
public class ComplaintController {

    @Operation(summary = "쓰레기통 신고 작성")
    @PostMapping
    public void createComplaint(@CurrentUser String email, @Valid @RequestBody CreateComplaintRequest request) {
    }
}
