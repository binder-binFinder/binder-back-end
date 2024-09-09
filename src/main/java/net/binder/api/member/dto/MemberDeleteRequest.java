package net.binder.api.member.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MemberDeleteRequest {

    private final String input;
}
