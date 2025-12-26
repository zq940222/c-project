package com.cfileprocessor.engine.rule;

import com.cfileprocessor.model.entity.RuleDefinition;
import com.cfileprocessor.model.vo.CheckResult;
import com.cfileprocessor.model.vo.CheckResult.*;
import com.cfileprocessor.model.vo.ParseResult;
import com.cfileprocessor.model.vo.ParseResult.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

/**
 * 规则引擎 - 简化版本用于POC演示
 * 支持规则类型：唯一性检查、范围检查、正则匹配、枚举检查
 */
@Slf4j
@Component
public class RuleEngine {

    // 规则DSL解析正则
    private static final Pattern UNIQUE_RULE = Pattern.compile("(\\S+)\\s+(\\S+)\\s+不重复");
    private static final Pattern COMPARE_RULE = Pattern.compile("(\\S+)\\s+(\\S+)\\s*([><=!]+)\\s*(.+)");
    private static final Pattern MATCH_RULE = Pattern.compile("(\\S+)\\s+(\\S+)\\s+MATCH\\s+(.+)");
    private static final Pattern IN_RULE = Pattern.compile("(\\S+)\\s+(\\S+)\\s+IN\\s*\\(([^)]+)\\)");

    public CheckResult execute(List<RuleDefinition> rules, ParseResult parseResult) {
        CheckResult result = new CheckResult();
        result.setBatchId(UUID.randomUUID().toString().substring(0, 8));

        int errorCount = 0;
        int warningCount = 0;
        int passCount = 0;

        for (RuleDefinition rule : rules) {
            if (!"PUBLISHED".equals(rule.getStatus())) {
                continue;
            }

            List<CheckItem> items = executeRule(rule, parseResult);

            for (CheckItem item : items) {
                result.getResults().add(item);
                if ("ERROR".equals(item.getResultType())) {
                    errorCount++;
                } else if ("WARNING".equals(item.getResultType())) {
                    warningCount++;
                } else {
                    passCount++;
                }
            }
        }

        result.getSummary().setErrorCount(errorCount);
        result.getSummary().setWarningCount(warningCount);
        result.getSummary().setPassCount(passCount);

        return result;
    }

    private List<CheckItem> executeRule(RuleDefinition rule, ParseResult parseResult) {
        List<CheckItem> items = new ArrayList<>();
        String dsl = rule.getDslContent().trim();

        try {
            // 解析规则DSL
            Matcher uniqueMatcher = UNIQUE_RULE.matcher(dsl);
            Matcher compareMatcher = COMPARE_RULE.matcher(dsl);
            Matcher matchMatcher = MATCH_RULE.matcher(dsl);
            Matcher inMatcher = IN_RULE.matcher(dsl);

            if (uniqueMatcher.matches()) {
                items.addAll(checkUnique(rule, uniqueMatcher.group(1), uniqueMatcher.group(2), parseResult));
            } else if (compareMatcher.matches()) {
                items.addAll(checkCompare(rule, compareMatcher.group(1), compareMatcher.group(2),
                        compareMatcher.group(3), compareMatcher.group(4).trim(), parseResult));
            } else if (matchMatcher.matches()) {
                items.addAll(checkMatch(rule, matchMatcher.group(1), matchMatcher.group(2),
                        matchMatcher.group(3).trim(), parseResult));
            } else if (inMatcher.matches()) {
                items.addAll(checkIn(rule, inMatcher.group(1), inMatcher.group(2),
                        inMatcher.group(3), parseResult));
            } else {
                log.warn("无法解析规则DSL: {}", dsl);
            }
        } catch (Exception e) {
            log.error("执行规则失败: {}", e.getMessage());
            CheckItem item = new CheckItem();
            item.setRuleId(rule.getId());
            item.setRuleName(rule.getName());
            item.setResultType("ERROR");
            item.setMessage("规则执行失败: " + e.getMessage());
            items.add(item);
        }

        return items;
    }

    /**
     * 唯一性检查
     */
    private List<CheckItem> checkUnique(RuleDefinition rule, String varName, String fieldName,
                                        ParseResult parseResult) {
        List<CheckItem> items = new ArrayList<>();

        // 查找目标变量
        VariableInfo variable = findVariable(varName, parseResult);
        if (variable == null) {
            return items;
        }

        // 统计字段值出现次数
        Map<Object, List<Integer>> valuePositions = new LinkedHashMap<>();
        List<Object> dataList = variable.getData();

        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                Object value = map.get(fieldName);
                if (value != null) {
                    valuePositions.computeIfAbsent(value, k -> new ArrayList<>()).add(i + 1);
                }
            }
        }

        // 检查重复
        for (Map.Entry<Object, List<Integer>> entry : valuePositions.entrySet()) {
            if (entry.getValue().size() > 1) {
                CheckItem item = new CheckItem();
                item.setRuleId(rule.getId());
                item.setRuleName(rule.getName());
                item.setResultType(rule.getSeverity() != null ? rule.getSeverity() : "ERROR");
                item.setMessage(String.format("%s \"%s\" 重复出现，位置: %s",
                        fieldName, entry.getKey(), entry.getValue()));

                SourceLocation loc = new SourceLocation();
                loc.setStartLine(variable.getLocation().getStartLine() + entry.getValue().get(0));
                loc.setEndLine(loc.getStartLine());
                item.setLocation(loc);

                items.add(item);
            }
        }

        return items;
    }

    /**
     * 比较检查 (>, <, >=, <=, =, !=)
     */
    private List<CheckItem> checkCompare(RuleDefinition rule, String varName, String fieldName,
                                         String operator, String valueStr, ParseResult parseResult) {
        List<CheckItem> items = new ArrayList<>();

        VariableInfo variable = findVariable(varName, parseResult);
        if (variable == null) {
            return items;
        }

        Object targetValue = parseValue(valueStr);
        List<Object> dataList = variable.getData();

        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                Object actualValue = map.get(fieldName);

                if (!compareValues(actualValue, operator, targetValue)) {
                    CheckItem item = new CheckItem();
                    item.setRuleId(rule.getId());
                    item.setRuleName(rule.getName());
                    item.setResultType(rule.getSeverity() != null ? rule.getSeverity() : "ERROR");
                    item.setMessage(String.format("第%d项 %s=\"%s\" 不满足条件 %s %s",
                            i + 1, fieldName, actualValue, operator, valueStr));

                    SourceLocation loc = new SourceLocation();
                    loc.setStartLine(variable.getLocation().getStartLine() + i + 1);
                    loc.setEndLine(loc.getStartLine());
                    item.setLocation(loc);

                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * 正则匹配检查
     */
    private List<CheckItem> checkMatch(RuleDefinition rule, String varName, String fieldName,
                                       String regex, ParseResult parseResult) {
        List<CheckItem> items = new ArrayList<>();

        VariableInfo variable = findVariable(varName, parseResult);
        if (variable == null) {
            return items;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            CheckItem item = new CheckItem();
            item.setRuleId(rule.getId());
            item.setRuleName(rule.getName());
            item.setResultType("ERROR");
            item.setMessage("正则表达式语法错误: " + regex);
            items.add(item);
            return items;
        }

        List<Object> dataList = variable.getData();
        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                Object actualValue = map.get(fieldName);

                if (actualValue != null && !pattern.matcher(String.valueOf(actualValue)).matches()) {
                    CheckItem item = new CheckItem();
                    item.setRuleId(rule.getId());
                    item.setRuleName(rule.getName());
                    item.setResultType(rule.getSeverity() != null ? rule.getSeverity() : "ERROR");
                    item.setMessage(String.format("第%d项 %s=\"%s\" 不匹配正则 %s",
                            i + 1, fieldName, actualValue, regex));

                    SourceLocation loc = new SourceLocation();
                    loc.setStartLine(variable.getLocation().getStartLine() + i + 1);
                    loc.setEndLine(loc.getStartLine());
                    item.setLocation(loc);

                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * 枚举检查
     */
    private List<CheckItem> checkIn(RuleDefinition rule, String varName, String fieldName,
                                    String valuesStr, ParseResult parseResult) {
        List<CheckItem> items = new ArrayList<>();

        VariableInfo variable = findVariable(varName, parseResult);
        if (variable == null) {
            return items;
        }

        // 解析枚举值
        Set<String> allowedValues = new HashSet<>();
        for (String v : valuesStr.split(",")) {
            String trimmed = v.trim();
            // 去除引号
            if ((trimmed.startsWith("'") && trimmed.endsWith("'")) ||
                (trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            allowedValues.add(trimmed);
        }

        List<Object> dataList = variable.getData();
        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                Object actualValue = map.get(fieldName);

                if (actualValue != null && !allowedValues.contains(String.valueOf(actualValue))) {
                    CheckItem item = new CheckItem();
                    item.setRuleId(rule.getId());
                    item.setRuleName(rule.getName());
                    item.setResultType(rule.getSeverity() != null ? rule.getSeverity() : "ERROR");
                    item.setMessage(String.format("第%d项 %s=\"%s\" 不在允许值列表中 (%s)",
                            i + 1, fieldName, actualValue, valuesStr));

                    SourceLocation loc = new SourceLocation();
                    loc.setStartLine(variable.getLocation().getStartLine() + i + 1);
                    loc.setEndLine(loc.getStartLine());
                    item.setLocation(loc);

                    items.add(item);
                }
            }
        }

        return items;
    }

    private VariableInfo findVariable(String varName, ParseResult parseResult) {
        for (VariableInfo var : parseResult.getVariables()) {
            if (var.getName().equals(varName)) {
                return var;
            }
        }
        return null;
    }

    private Object parseValue(String valueStr) {
        // 去除引号
        if ((valueStr.startsWith("'") && valueStr.endsWith("'")) ||
            (valueStr.startsWith("\"") && valueStr.endsWith("\""))) {
            return valueStr.substring(1, valueStr.length() - 1);
        }

        // 尝试解析为数字
        try {
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            }
            return Long.parseLong(valueStr);
        } catch (NumberFormatException e) {
            return valueStr;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean compareValues(Object actual, String operator, Object target) {
        if (actual == null) {
            return false;
        }

        // 转换为可比较的类型
        if (actual instanceof Number && target instanceof Number) {
            double actualNum = ((Number) actual).doubleValue();
            double targetNum = ((Number) target).doubleValue();

            return switch (operator) {
                case ">" -> actualNum > targetNum;
                case "<" -> actualNum < targetNum;
                case ">=" -> actualNum >= targetNum;
                case "<=" -> actualNum <= targetNum;
                case "=", "==" -> actualNum == targetNum;
                case "!=", "<>" -> actualNum != targetNum;
                default -> false;
            };
        } else {
            String actualStr = String.valueOf(actual);
            String targetStr = String.valueOf(target);

            return switch (operator) {
                case "=", "==" -> actualStr.equals(targetStr);
                case "!=", "<>" -> !actualStr.equals(targetStr);
                default -> {
                    int cmp = actualStr.compareTo(targetStr);
                    yield switch (operator) {
                        case ">" -> cmp > 0;
                        case "<" -> cmp < 0;
                        case ">=" -> cmp >= 0;
                        case "<=" -> cmp <= 0;
                        default -> false;
                    };
                }
            };
        }
    }
}
