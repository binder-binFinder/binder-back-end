package net.binder.api.filtering.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class OpenAiRequest {
    private final String model;
    private final List<OpenAiMessage> messages;

    public OpenAiRequest(String model, String target) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new OpenAiMessage("user", target));
    }
}
