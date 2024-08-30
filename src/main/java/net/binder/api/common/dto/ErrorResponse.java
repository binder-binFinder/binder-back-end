package net.binder.api.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {

    private final String message;

    public static ErrorResponse from(String message) {
        return new ErrorResponse(message);
    }
}
