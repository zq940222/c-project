package com.cfileprocessor.model.vo;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class ParseResult {
    private String fileId;
    private String fileName;
    private List<StructInfo> structs = new ArrayList<>();
    private List<VariableInfo> variables = new ArrayList<>();
    private List<MacroInfo> macros = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    @Data
    public static class StructInfo {
        private Long id;
        private String name;
        private List<FieldInfo> fields = new ArrayList<>();
        private SourceLocation location;
    }

    @Data
    public static class FieldInfo {
        private String name;
        private String type;
        private boolean isPointer;
        private boolean isArray;
        private Integer arraySize;
    }

    @Data
    public static class VariableInfo {
        private Long id;
        private String name;
        private String structType;
        private int count;
        private List<Object> data;
        private SourceLocation location;
    }

    @Data
    public static class MacroInfo {
        private String name;
        private String value;
        private SourceLocation location;
    }

    @Data
    public static class SourceLocation {
        private int startLine;
        private int endLine;
        private int startColumn;
        private int endColumn;
    }
}
