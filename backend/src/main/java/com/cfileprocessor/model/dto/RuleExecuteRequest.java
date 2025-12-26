package com.cfileprocessor.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class RuleExecuteRequest {
    private List<Long> ruleIds;
    private String content;
    private String fileName;
}
