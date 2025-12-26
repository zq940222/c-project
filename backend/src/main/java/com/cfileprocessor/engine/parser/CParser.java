package com.cfileprocessor.engine.parser;

import com.cfileprocessor.model.vo.ParseResult;
import com.cfileprocessor.model.vo.ParseResult.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

/**
 * C语言解析器 - 简化版本用于POC演示
 * 支持解析：结构体定义、变量声明、宏定义
 */
@Slf4j
@Component
public class CParser {

    // 宏定义正则: #define NAME VALUE
    private static final Pattern MACRO_PATTERN = Pattern.compile(
            "#define\\s+(\\w+)(?:\\s+(.+))?",
            Pattern.MULTILINE
    );

    // 结构体定义正则: struct Name { fields }
    private static final Pattern STRUCT_PATTERN = Pattern.compile(
            "struct\\s+(\\w+)\\s*\\{([^}]+)\\}",
            Pattern.DOTALL
    );

    // 字段定义正则: type name; 或 type* name; 或 type name[size];
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\s*(\\w+)(\\s*\\*)?\\s+(\\w+)(?:\\[(\\d+)\\])?\\s*;",
            Pattern.MULTILINE
    );

    // 变量声明正则: StructName varName = { ... } 或 StructName varName[] = { ... }
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(
            "(\\w+)\\s+(\\w+)\\s*(\\[\\s*\\d*\\s*\\])?\\s*=\\s*\\{([^;]+)\\}\\s*;",
            Pattern.DOTALL
    );

    // 数组元素正则: { field1, field2, ... }
    private static final Pattern ARRAY_ELEMENT_PATTERN = Pattern.compile(
            "\\{([^{}]+)\\}",
            Pattern.DOTALL
    );

    public ParseResult parse(String content, String fileName) {
        ParseResult result = new ParseResult();
        result.setFileId(UUID.randomUUID().toString().substring(0, 8));
        result.setFileName(fileName);

        String[] lines = content.split("\n");

        try {
            // 1. 解析宏定义
            parseMacros(content, lines, result);

            // 2. 解析结构体定义
            parseStructs(content, lines, result);

            // 3. 解析变量声明
            parseVariables(content, lines, result);

        } catch (Exception e) {
            log.error("解析错误: {}", e.getMessage());
            result.getErrors().add("解析错误: " + e.getMessage());
        }

        return result;
    }

    private void parseMacros(String content, String[] lines, ParseResult result) {
        Matcher matcher = MACRO_PATTERN.matcher(content);
        while (matcher.find()) {
            MacroInfo macro = new MacroInfo();
            macro.setName(matcher.group(1));
            macro.setValue(matcher.group(2) != null ? matcher.group(2).trim() : "");

            // 计算行号
            int lineNum = getLineNumber(content, matcher.start());
            SourceLocation loc = new SourceLocation();
            loc.setStartLine(lineNum);
            loc.setEndLine(lineNum);
            macro.setLocation(loc);

            result.getMacros().add(macro);
            log.debug("解析到宏定义: {} = {}", macro.getName(), macro.getValue());
        }
    }

    private void parseStructs(String content, String[] lines, ParseResult result) {
        Matcher matcher = STRUCT_PATTERN.matcher(content);
        long structId = 1;

        while (matcher.find()) {
            StructInfo struct = new StructInfo();
            struct.setId(structId++);
            struct.setName(matcher.group(1));

            // 解析字段
            String fieldsContent = matcher.group(2);
            Matcher fieldMatcher = FIELD_PATTERN.matcher(fieldsContent);
            while (fieldMatcher.find()) {
                FieldInfo field = new FieldInfo();
                field.setType(fieldMatcher.group(1));
                field.setPointer(fieldMatcher.group(2) != null);
                field.setName(fieldMatcher.group(3));
                if (fieldMatcher.group(4) != null) {
                    field.setArray(true);
                    field.setArraySize(Integer.parseInt(fieldMatcher.group(4)));
                }
                struct.getFields().add(field);
            }

            // 计算位置
            int startLine = getLineNumber(content, matcher.start());
            int endLine = getLineNumber(content, matcher.end());
            SourceLocation loc = new SourceLocation();
            loc.setStartLine(startLine);
            loc.setEndLine(endLine);
            struct.setLocation(loc);

            result.getStructs().add(struct);
            log.debug("解析到结构体: {} 包含 {} 个字段", struct.getName(), struct.getFields().size());
        }
    }

    private void parseVariables(String content, String[] lines, ParseResult result) {
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        long varId = 1;

        while (matcher.find()) {
            VariableInfo variable = new VariableInfo();
            variable.setId(varId++);
            variable.setStructType(matcher.group(1));
            variable.setName(matcher.group(2));

            boolean isArray = matcher.group(3) != null;
            String dataContent = matcher.group(4).trim();

            // 解析数据
            List<Object> dataList = new ArrayList<>();
            if (isArray) {
                // 数组变量，解析多个元素
                Matcher elementMatcher = ARRAY_ELEMENT_PATTERN.matcher(dataContent);
                while (elementMatcher.find()) {
                    Map<String, Object> item = parseDataItem(elementMatcher.group(1), result);
                    if (item != null) {
                        dataList.add(item);
                    }
                }
            } else {
                // 单个变量
                Map<String, Object> item = parseDataItem(dataContent, result);
                if (item != null) {
                    dataList.add(item);
                }
            }

            variable.setData(dataList);
            variable.setCount(dataList.size());

            // 计算位置
            int startLine = getLineNumber(content, matcher.start());
            int endLine = getLineNumber(content, matcher.end());
            SourceLocation loc = new SourceLocation();
            loc.setStartLine(startLine);
            loc.setEndLine(endLine);
            variable.setLocation(loc);

            result.getVariables().add(variable);
            log.debug("解析到变量: {} 类型: {} 数量: {}", variable.getName(), variable.getStructType(), variable.getCount());
        }
    }

    private Map<String, Object> parseDataItem(String itemContent, ParseResult result) {
        Map<String, Object> item = new LinkedHashMap<>();

        // 按逗号分割，但要处理引号内的逗号
        List<String> values = splitByComma(itemContent.trim());

        // 尝试找到对应的结构体定义来获取字段名
        // 如果找不到，使用通用字段名
        List<String> fieldNames = new ArrayList<>();
        if (!result.getStructs().isEmpty()) {
            for (FieldInfo field : result.getStructs().get(0).getFields()) {
                fieldNames.add(field.getName());
            }
        }

        for (int i = 0; i < values.size(); i++) {
            String fieldName = i < fieldNames.size() ? fieldNames.get(i) : "field" + (i + 1);
            String value = values.get(i).trim();

            // 去除引号
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            // 尝试解析为数字
            Object parsedValue = parseValue(value);
            item.put(fieldName, parsedValue);
        }

        return item;
    }

    private List<String> splitByComma(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if ((c == '"' || c == '\'') && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }

    private Object parseValue(String value) {
        value = value.trim();

        // 十六进制
        if (value.toLowerCase().startsWith("0x")) {
            try {
                return Long.parseLong(value.substring(2), 16);
            } catch (NumberFormatException e) {
                return value;
            }
        }

        // 整数
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 不是整数
        }

        // 浮点数
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // 不是浮点数
        }

        // 字符串
        return value;
    }

    private int getLineNumber(String content, int position) {
        int line = 1;
        for (int i = 0; i < position && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
}
