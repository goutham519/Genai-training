package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.model.PromptResponse;
import com.epam.training.gen.ai.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prompt")
public class PromptController {
    private final PromptService promptService;

    /**
     * @param userPrompt Input from the User
     * @return Response of th eChatBot
     */
    @GetMapping(value = "/getResults")
    public PromptResponse getResults(
            @RequestParam String userPrompt) {

        return Optional.ofNullable(promptService)
                .map(promptService -> promptService.getChatBotResponse(userPrompt))
                .orElseGet(PromptResponse::new);
    }
}
