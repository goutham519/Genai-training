package com.epam.training.gen.ai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerations;
import com.epam.training.gen.ai.configuration.PromptConfiguration;
import com.epam.training.gen.ai.model.PromptResponse;
import com.epam.training.gen.ai.model.UserRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/** Sample demonstrates how to get the images for a given prompt. */
@Service
@AllArgsConstructor
public class ImageGeneratorService {

    private final OpenAIAsyncClient openAIAsyncClient;
    private final PromptConfiguration chatBotConfigurations;

    /** Runs the sample algorithm and demonstrates how to get the images for a given prompt. */
    public PromptResponse getResponse(UserRequest userRequest) {
        ImageGenerationOptions imageGenerationOptions =
                new ImageGenerationOptions(userRequest.getPrompt());
        ImageGenerations images =
                openAIAsyncClient
                        .getImageGenerations(chatBotConfigurations.getDeploymentName(), imageGenerationOptions)
                        .block();

        return Optional.ofNullable(images).map(ImageGenerations::getData).stream()
                .flatMap(Collection::stream)
                .map(ImageGenerationData::getUrl)
                .findFirst()
                .map(
                        imageUrl ->
                                PromptResponse.builder()
                                        .userPrompt(userRequest.getPrompt())
                                        .aiResponse(imageUrl)
                                        .build())
                .orElse(PromptResponse.builder().aiResponse("No Response from AI..!").build());
    }
}