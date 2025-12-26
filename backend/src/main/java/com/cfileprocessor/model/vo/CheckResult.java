package com.cfileprocessor.model.vo;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class CheckResult {
    private String batchId;
    private List<CheckItem> results = new ArrayList<>();
    private CheckSummary summary = new CheckSummary();

    @Data
    public static class CheckItem {
        private Long ruleId;
        private String ruleName;
        private String resultType; // ERROR, WARNING, INFO, PASS
        private String message;
        private ParseResult.SourceLocation location;
        private String contextCode;
    }

    @Data
    public static class CheckSummary {
        private int errorCount;
        private int warningCount;
        private int passCount;
    }
}
