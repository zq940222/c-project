package com.cfileprocessor.model.dto;

import lombok.Data;

@Data
public class DiffRequest {
    private String leftContent;
    private String rightContent;
    private String leftFileName;
    private String rightFileName;
    private DiffOptions options;

    @Data
    public static class DiffOptions {
        private boolean ignoreWhitespace = true;
        private boolean ignoreComments = true;
        private boolean ignoreOrder = true;
        private boolean ignoreNumberFormat = true;
        private boolean semanticCompare = true;
    }
}
