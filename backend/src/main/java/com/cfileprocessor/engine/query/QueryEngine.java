package com.cfileprocessor.engine.query;

import com.cfileprocessor.model.vo.ParseResult;
import com.cfileprocessor.model.vo.ParseResult.*;
import com.cfileprocessor.model.vo.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * 查询引擎 - 简化版本用于POC演示
 * 支持DSL: SEARCH 变量 [WHERE 条件] [ORDER BY 字段] [LIMIT n]
 */
@Slf4j
@Component
public class QueryEngine {

    // DSL解析正则
    private static final Pattern SEARCH_PATTERN = Pattern.compile(
            "SEARCH\\s+(\\S+)" +
            "(?:\\s+WHERE\\s+(.+?))?" +
            "(?:\\s+ORDER\\s+BY\\s+(\\S+)(?:\\s+(ASC|DESC))?)?" +
            "(?:\\s+LIMIT\\s+(\\d+)(?:\\s+OFFSET\\s+(\\d+))?)?",
            Pattern.CASE_INSENSITIVE
    );

    // 条件解析正则
    private static final Pattern CONDITION_PATTERN = Pattern.compile(
            "(\\S+)\\s*([><=!]+|LIKE|IN)\\s*(.+)",
            Pattern.CASE_INSENSITIVE
    );

    public QueryResult execute(String dsl, ParseResult parseResult) {
        QueryResult result = new QueryResult();
        long startTime = System.currentTimeMillis();

        try {
            Matcher matcher = SEARCH_PATTERN.matcher(dsl.trim());
            if (!matcher.matches()) {
                log.warn("无效的查询DSL: {}", dsl);
                return result;
            }

            String varName = matcher.group(1);
            String whereClause = matcher.group(2);
            String orderField = matcher.group(3);
            String orderDir = matcher.group(4);
            String limitStr = matcher.group(5);
            String offsetStr = matcher.group(6);

            // 查找变量
            VariableInfo variable = findVariable(varName, parseResult);
            if (variable == null) {
                log.warn("未找到变量: {}", varName);
                return result;
            }

            // 查找结构体定义获取列名
            StructInfo struct = findStruct(variable.getStructType(), parseResult);
            if (struct != null) {
                for (FieldInfo field : struct.getFields()) {
                    result.getColumns().add(field.getName());
                }
            }
            result.getColumns().add("_row");
            result.getColumns().add("_line");

            // 获取数据
            List<Map<String, Object>> rows = new ArrayList<>();
            int rowNum = 1;
            for (Object data : variable.getData()) {
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> row = new LinkedHashMap<>((Map<String, Object>) data);
                    row.put("_row", rowNum);
                    row.put("_line", variable.getLocation().getStartLine() + rowNum);
                    rows.add(row);
                }
                rowNum++;
            }

            // 应用WHERE条件
            if (whereClause != null && !whereClause.trim().isEmpty()) {
                rows = filterByConditions(rows, whereClause);
            }

            // 应用ORDER BY
            if (orderField != null && !orderField.trim().isEmpty()) {
                boolean ascending = !"DESC".equalsIgnoreCase(orderDir);
                rows = sortRows(rows, orderField, ascending);
            }

            // 记录总数
            result.setTotalCount(rows.size());

            // 应用LIMIT和OFFSET
            int offset = offsetStr != null ? Integer.parseInt(offsetStr) : 0;
            int limit = limitStr != null ? Integer.parseInt(limitStr) : rows.size();

            if (offset > 0 || limit < rows.size()) {
                int endIndex = Math.min(offset + limit, rows.size());
                if (offset < rows.size()) {
                    rows = rows.subList(offset, endIndex);
                } else {
                    rows = new ArrayList<>();
                }
            }

            result.setRows(rows);

        } catch (Exception e) {
            log.error("查询执行错误: {}", e.getMessage());
        }

        result.setExecutionTime(System.currentTimeMillis() - startTime);
        return result;
    }

    private List<Map<String, Object>> filterByConditions(List<Map<String, Object>> rows, String whereClause) {
        // 解析AND/OR条件
        String[] andParts = whereClause.split("(?i)\\s+AND\\s+");

        return rows.stream()
                .filter(row -> {
                    for (String part : andParts) {
                        // 检查是否有OR条件
                        String[] orParts = part.split("(?i)\\s+OR\\s+");
                        boolean orResult = false;

                        for (String condition : orParts) {
                            if (evaluateCondition(row, condition.trim())) {
                                orResult = true;
                                break;
                            }
                        }

                        if (!orResult) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private boolean evaluateCondition(Map<String, Object> row, String condition) {
        Matcher matcher = CONDITION_PATTERN.matcher(condition);
        if (!matcher.matches()) {
            return true;
        }

        String field = matcher.group(1);
        String operator = matcher.group(2).toUpperCase();
        String valueStr = matcher.group(3).trim();

        Object actual = row.get(field);
        if (actual == null) {
            return false;
        }

        // 去除引号
        if ((valueStr.startsWith("'") && valueStr.endsWith("'")) ||
            (valueStr.startsWith("\"") && valueStr.endsWith("\""))) {
            valueStr = valueStr.substring(1, valueStr.length() - 1);
        }

        String actualStr = String.valueOf(actual);

        return switch (operator) {
            case "=" -> actualStr.equals(valueStr);
            case "!=" -> !actualStr.equals(valueStr);
            case "LIKE" -> {
                String regex = valueStr.replace("%", ".*").replace("_", ".");
                yield actualStr.matches(regex);
            }
            case "IN" -> {
                String valuesContent = valueStr;
                if (valuesContent.startsWith("(") && valuesContent.endsWith(")")) {
                    valuesContent = valuesContent.substring(1, valuesContent.length() - 1);
                }
                Set<String> allowedValues = new HashSet<>();
                for (String v : valuesContent.split(",")) {
                    String trimmed = v.trim();
                    if ((trimmed.startsWith("'") && trimmed.endsWith("'")) ||
                        (trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
                        trimmed = trimmed.substring(1, trimmed.length() - 1);
                    }
                    allowedValues.add(trimmed);
                }
                yield allowedValues.contains(actualStr);
            }
            default -> {
                // 数值比较
                try {
                    double actualNum = Double.parseDouble(actualStr);
                    double targetNum = Double.parseDouble(valueStr);
                    yield switch (operator) {
                        case ">" -> actualNum > targetNum;
                        case "<" -> actualNum < targetNum;
                        case ">=" -> actualNum >= targetNum;
                        case "<=" -> actualNum <= targetNum;
                        default -> false;
                    };
                } catch (NumberFormatException e) {
                    int cmp = actualStr.compareTo(valueStr);
                    yield switch (operator) {
                        case ">" -> cmp > 0;
                        case "<" -> cmp < 0;
                        case ">=" -> cmp >= 0;
                        case "<=" -> cmp <= 0;
                        default -> false;
                    };
                }
            }
        };
    }

    private List<Map<String, Object>> sortRows(List<Map<String, Object>> rows, String field, boolean ascending) {
        return rows.stream()
                .sorted((a, b) -> {
                    Object valA = a.get(field);
                    Object valB = b.get(field);

                    if (valA == null && valB == null) return 0;
                    if (valA == null) return ascending ? -1 : 1;
                    if (valB == null) return ascending ? 1 : -1;

                    int cmp;
                    if (valA instanceof Number && valB instanceof Number) {
                        cmp = Double.compare(((Number) valA).doubleValue(), ((Number) valB).doubleValue());
                    } else {
                        cmp = String.valueOf(valA).compareTo(String.valueOf(valB));
                    }

                    return ascending ? cmp : -cmp;
                })
                .collect(Collectors.toList());
    }

    private VariableInfo findVariable(String varName, ParseResult parseResult) {
        for (VariableInfo var : parseResult.getVariables()) {
            if (var.getName().equals(varName)) {
                return var;
            }
        }
        return null;
    }

    private StructInfo findStruct(String structName, ParseResult parseResult) {
        for (StructInfo struct : parseResult.getStructs()) {
            if (struct.getName().equals(structName)) {
                return struct;
            }
        }
        return null;
    }
}
