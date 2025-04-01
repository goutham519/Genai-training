package com.epam.training.gen.ai.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@Builder
@ToString
public class UserRequest {
    private String prompt;
    private Double temperature;
    private int maxTokens;
    private String deploymentName;
    private List<String> stopSequences;
}
