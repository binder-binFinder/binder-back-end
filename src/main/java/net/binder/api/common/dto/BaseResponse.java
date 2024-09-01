package net.binder.api.common.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BaseResponse {

    private final Long id;

    private final LocalDateTime createAt;

    private final LocalDateTime modifiedAt;

}
