package net.binder.api.admin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.complaint.entity.ComplaintType;

@RequiredArgsConstructor
@Getter
public class TypeCount {
    private final ComplaintType type;
    private final Long count;
}
