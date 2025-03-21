package com.epam.training.gen.ai.history;

import com.epam.training.gen.ai.model.Chat;
import com.epam.training.gen.ai.model.PromptResponse;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for interacting with the AI kernel, maintaining chat history.
 *
 * <p>This service provides a method to process user prompts while preserving chat history. It uses
 * the {@link Kernel} to invoke AI responses based on the user's input and the previous chat
 * context. The conversation history is updated after each interaction.
 */
@Slf4j
@Service
@AllArgsConstructor
public class SimpleKernelHistory {
    private final Kernel kernel;

    public PromptResponse processWithHistory(Chat chat) {

        var chatHistory = new ChatHistory();
        String prompt = Optional.ofNullable(chat.getPrompt()).orElseThrow();
        var response =
                kernel
                        .invokeAsync(getChat())
                        .withArguments(getKernelFunctionArguments(prompt, chatHistory))
                        .withPromptExecutionSettings(
                                PromptExecutionSettings.builder()
                                        .withTemperature(Optional.ofNullable(chat.getTemperature()).orElse(0D))
                                        .withMaxTokens(Optional.of(chat.getMaxTokens()).orElse(1000))
                                        .withStopSequences(
                                                Optional.ofNullable(chat.getStopSequences()).orElse(List.of()))
                                        .build())
                        .block();
        String result =
                Optional.ofNullable(response).map(FunctionResult::getResult).orElse("No Response..!");
        chatHistory.addUserMessage(prompt);
        chatHistory.addAssistantMessage(result);
        chatHistory.addUserMessage("What do you know about me ?");
        chatHistory.addAssistantMessage(
                "I know about you that your name is Teja Mitte and you're a java developer.");
        chatHistory.forEach(chatMessageContent -> log.info(chatMessageContent.getContent()));
        log.info("AI answer : {}", result);
        return PromptResponse.builder().userPrompt(prompt).chatBotResponse(result).build();
    }

    /**
     * Creates a kernel function for generating a chat response using a predefined prompt template.
     *
     * <p>The template includes the chat history and the user's message as variables.
     *
     * @return a {@link KernelFunction} for handling chat-based AI interactions
     */
    private KernelFunction<String> getChat() {
        return KernelFunction.<String>createFromPrompt(
                        """
                                    {{$chatHistory}}
                                    <message role="user">{{$request}}</message>""")
                .build();
    }

    /**
     * Creates the kernel function arguments with the user prompt and chat history.
     *
     * @param prompt the user's input
     * @param chatHistory the current chat history
     * @return a {@link KernelFunctionArguments} instance containing the variables for the AI model
     */
    private KernelFunctionArguments getKernelFunctionArguments(
            String prompt, ChatHistory chatHistory) {
        return KernelFunctionArguments.builder()
                .withVariable("request", prompt)
                .withVariable("chatHistory", chatHistory)
                .build();
    }
}
