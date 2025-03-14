package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.model.PromptResponse;
import com.epam.training.gen.ai.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PromptController {
    private final PromptService promptService;

    /**
     * @param userPrompt Input from the User
     * @param integratePlugin To work with Mobile Phones plugin
     * @return Response of th eChatBot
     */
    @GetMapping(value = "/getResponse")
    public PromptResponse getGeneratedResponse(
            @RequestParam String userPrompt, @RequestParam Boolean integratePlugin) {

        return Optional.ofNullable(promptService)
                .map(promptService -> promptService.getChatBotResponse(userPrompt, integratePlugin))
                .orElseGet(PromptResponse::new);
    }
}
