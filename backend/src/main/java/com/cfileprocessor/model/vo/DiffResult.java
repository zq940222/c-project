package com.cfileprocessor.model.vo;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class DiffResult {
    private List<DiffItem> items = new ArrayList<>();
    private DiffSummary summary = new DiffSummary();
    private String unifiedDiff;

    @Data
    public static class DiffItem {
        private String type; // ADDED, REMOVED, MODIFIED, FORMAT_ONLY, ORDER_ONLY
        private ParseResult.SourceLocation leftLocation;
        private ParseResult.SourceLocation rightLocation;
        private String leftContent;
        private String rightContent;
        private boolean ignorable;
        private String ignoreReason;
    }

    @Data
    public static class DiffSummary {
        private int totalDiffs;
        private int significantDiffs;
        private int ignorableDiffs;
        private int added;
        private int removed;
        private int modified;
    }
}
