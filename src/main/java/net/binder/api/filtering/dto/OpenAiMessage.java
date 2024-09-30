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
                "\"%s\" 큰따옴표 안에 비속어가 포함되어 있는지 여부(isCurse), 해당 문장 내에서 욕으로 판단한 단어목록(words) 원본(욕 사이에 1이나 다른 문자가 포함되어 있다면 그대로 포함)을 json형식으로 답해줘. 응답 시 백틱이나 다른 문자를 포함하지 말고, 순수한 JSON 형식으로만 응답해줘.",
                target);
    }
}
