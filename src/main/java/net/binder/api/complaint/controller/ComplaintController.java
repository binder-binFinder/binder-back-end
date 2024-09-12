package net.binder.api.complaint.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.complaint.dto.CreateComplaintRequest;
import net.binder.api.complaint.service.ComplaintService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/complaints")
@Tag(name = "쓰레기통 신고 관리")
public class ComplaintController {

    private final ComplaintService complaintService;

    @Operation(summary = "쓰레기통 신고 작성")
    @PostMapping
    public void createComplaint(@CurrentUser String email, @Valid @RequestBody CreateComplaintRequest request) {
        complaintService.createComplaint(email, request.getBinId(), request.getComplaintType());
    }
}
