package net.binder.api.filtering.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CurseCheckResult {

    private final Boolean isCurse;

    private final List<String> words;

    private final Boolean isAiChecked;

    public boolean isCurse() {
        return isCurse;
    }

    public boolean isAiChecked() {
        return isAiChecked;
    }
}
