package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.configuration.PromptConfiguration;
import com.epam.training.gen.ai.model.Chat;
import com.epam.training.gen.ai.model.PromptResponse;
import com.epam.training.gen.ai.model.UserRequest;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PromptService {
    private final ChatCompletionService chatCompletionService;
    private final Kernel kernel;
    private final InvocationContext invocationContext;
    private final ChatHistory chatHistory;
    private final PromptConfiguration chatBotConfigurations;

    public PromptResponse getChatBotResponse(String userPrompt) {

        ChatHistory history = new ChatHistory();
        history.addUserMessage(userPrompt);
        List<ChatMessageContent<?>> results = chatCompletionService
                .getChatMessageContentsAsync(history, null, invocationContext)
                .block();
        String response =
                Optional.ofNullable(results).orElse(List.of()).stream()
                        .filter(
                                chatMessageContent ->
                                        chatMessageContent.getAuthorRole() == AuthorRole.ASSISTANT
                                                && chatMessageContent.getContent() != null)
                        .map(ChatMessageContent::getContent)
                        .findFirst()
                        .orElse("No Output, Something is wrong . . !");
        return PromptResponse.builder().userPrompt(userPrompt).aiResponse(response).build();
    }

    public PromptResponse processWithHistory(UserRequest userRequest) {

        String prompt = Optional.ofNullable(userRequest.getPrompt()).orElseThrow();
        chatHistory.addUserMessage(prompt);
        var response =
                kernel
                        .invokeAsync(getChat())
                        .withArguments(getKernelFunctionArguments(prompt, chatHistory))
                        .withPromptExecutionSettings(
                                PromptExecutionSettings.builder()
                                        .withTemperature(Optional.ofNullable(userRequest.getTemperature()).orElse(0.5D))
                                        .withMaxTokens(
                                                Optional.of(userRequest.getMaxTokens())
                                                        .filter(tokenValue -> tokenValue != 0)
                                                        .orElse(1000))
                                        .withStopSequences(
                                                Optional.ofNullable(userRequest.getStopSequences()).orElse(List.of()))
                                        .withModelId(
                                                Optional.ofNullable(userRequest.getDeploymentName())
                                                        .orElse(chatBotConfigurations.getDeploymentName()))
                                        .build())
                        .block();
        String result =
                Optional.ofNullable(response).map(FunctionResult::getResult).orElse("No Response..!");
        chatHistory.addAssistantMessage(result);
        return PromptResponse.builder().userPrompt(prompt).aiResponse(getBotResponse(chatHistory)).build();
    }

    private String getBotResponse(ChatHistory chatHistory) {
        return Optional.of(
                        chatHistory.getMessages().stream()
                                .map(
                                        chatMessageContent -> {
                                            String botResponse =
                                                    " ***** " + chatMessageContent.getAuthorRole().name() + ":";
                                            botResponse += chatMessageContent.getContent();
                                            return botResponse;
                                        })
                                .collect(Collectors.joining(" *****   ")))
                .orElse("No Response");
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