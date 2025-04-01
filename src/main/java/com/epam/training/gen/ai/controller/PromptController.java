package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.history.SimpleKernelHistory;
import com.epam.training.gen.ai.model.Chat;
import com.epam.training.gen.ai.model.PromptResponse;
import com.epam.training.gen.ai.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prompt")
public class PromptController {
    private final PromptService promptService;
    private final SimpleKernelHistory kernelHistory;

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
    @PostMapping(value = "/init-chat")
    public PromptResponse getResponseFromHistory(@RequestBody Chat chat) {
        return Optional.ofNullable(kernelHistory)
                .map(kernelHistory -> kernelHistory.processWithHistory(chat))
                .orElseGet(PromptResponse::new);
    }

}
