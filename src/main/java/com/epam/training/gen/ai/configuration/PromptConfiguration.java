package com.epam.training.gen.ai.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.epam.training.gen.ai.plugins.SimplePlugin;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@Getter
public class PromptConfiguration {

    @Value("${client-azureopenai-key}")
    private String AZURE_CLIENT_KEY;

    @Value("${client-azureopenai-endpoint}")
    private String CLIENT_ENDPOINT;

    @Value("${client-azureopenai-deployment-name}")
    private String deploymentName;

    public static final String API_KEY = "Api-Key";

    @Bean
    @Primary
    public OpenAIAsyncClient openAIAsyncClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();
    }

    @Bean
    public ChatCompletionService chatCompletionService() {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentName)
                .withOpenAIAsyncClient(openAIAsyncClient())
                .build();
    }

    // Mobile Phones Plugin
    @Bean
    public KernelPlugin kernelPlugin() {
        return KernelPluginFactory.createFromObject(new SimplePlugin(), "SimplePlugin");
    }

    //  kernel with Azure OpenAI chat completion and plugin
    @Bean
    public Kernel kernel() {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService())
                .withPlugin(kernelPlugin())
                .build();
    }

    // Enable planning
    @Bean
    public InvocationContext invocationContext() {
        return new InvocationContext.Builder()
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
                List.of(
                        (request, body, execution) -> {
                            request.getHeaders().set(API_KEY, AZURE_CLIENT_KEY);
                            return execution.execute(request, body);
                        }));
        return restTemplate;
    }
}