package net.binder.api.filtering.dto;

import lombok.Getter;

@Getter
public class OpenAiMessage {

    private final String role;

    private final String content;

    public OpenAiMessage(String role, String target) {
        this.role = role;
        this.content = getContent(target);
    }

    private String getContent(String target) {
        return String.format(
                "다음 문장에서 비속어가 포함되어 있는지 여부(isCurse)와, 비속어로 판단된 단어 목록(words)을 원본 그대로 포함하여 백틱이 없는 순수한 JSON 형식으로 응답해 주세요. \"%s\"",
                target);
    }
}
