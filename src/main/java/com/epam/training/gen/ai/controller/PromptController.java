package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.model.Chat;
import com.epam.training.gen.ai.model.PromptResponse;
import com.epam.training.gen.ai.model.UserRequest;
import com.epam.training.gen.ai.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prompt")
public class PromptController {
    private final PromptService promptService;
    private final RestTemplate restTemplate;

    @Value("${epam.dial.deployment-names-api}")
    private String DEPLOYMENT_NAMES_URL;

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
    public PromptResponse getResponseFromHistory(@RequestBody UserRequest userRequest) {
        return Optional.ofNullable(promptService)
                .map(promptService -> promptService.processWithHistory(userRequest))
                .orElseGet(PromptResponse::new);
    }

    @GetMapping(value = "getDeploymentNames")
    public ResponseEntity<String> getDeploymentNames() {
        return new ResponseEntity<>(
                Optional.ofNullable(restTemplate.getForObject(DEPLOYMENT_NAMES_URL, String.class))
                        .orElseThrow(),
                HttpStatus.OK);
    }

}
