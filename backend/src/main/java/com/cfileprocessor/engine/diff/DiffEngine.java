package com.cfileprocessor.engine.diff;

import com.cfileprocessor.model.dto.DiffRequest;
import com.cfileprocessor.model.vo.DiffResult;
import com.cfileprocessor.model.vo.DiffResult.*;
import com.cfileprocessor.model.vo.ParseResult.SourceLocation;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

/**
 * 比对引擎 - 三层比对架构
 * 1. 纯文本比对
 * 2. 语法比对（忽略空白、注释、数值格式）
 * 3. 业务比对（忽略顺序）
 */
@Slf4j
@Component
public class DiffEngine {

    // 注释正则
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("//.*$", Pattern.MULTILINE);
    private static final Pattern MULTI_LINE_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    // 十六进制数值正则
    private static final Pattern HEX_PATTERN = Pattern.compile("0[xX]0*([0-9a-fA-F]+)");

    // 数组元素正则
    private static final Pattern ARRAY_ELEMENT_PATTERN = Pattern.compile(
            "\\{([^{}]+)\\}",
            Pattern.DOTALL
    );

    public DiffResult compare(String leftContent, String rightContent, DiffRequest.DiffOptions options) {
        DiffResult result = new DiffResult();

        if (options == null) {
            options = new DiffRequest.DiffOptions();
        }

        // 预处理内容
        String processedLeft = leftContent;
        String processedRight = rightContent;

        if (options.isIgnoreComments()) {
            processedLeft = removeComments(processedLeft);
            processedRight = removeComments(processedRight);
        }

        if (options.isIgnoreNumberFormat()) {
            processedLeft = normalizeHexNumbers(processedLeft);
            processedRight = normalizeHexNumbers(processedRight);
        }

        // 分割为行
        List<String> leftLines = Arrays.asList(processedLeft.split("\n"));
        List<String> rightLines = Arrays.asList(processedRight.split("\n"));

        // 如果忽略空白，则规范化每行
        if (options.isIgnoreWhitespace()) {
            leftLines = normalizeWhitespace(leftLines);
            rightLines = normalizeWhitespace(rightLines);
        }

        // 执行diff
        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);

        // 生成unified diff
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "left.c", "right.c", leftLines, patch, 3
        );
        result.setUnifiedDiff(String.join("\n", unifiedDiff));

        // 处理每个delta
        int added = 0, removed = 0, modified = 0;
        int ignorable = 0;

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            DiffItem item = new DiffItem();

            // 设置位置
            SourceLocation leftLoc = new SourceLocation();
            leftLoc.setStartLine(delta.getSource().getPosition() + 1);
            leftLoc.setEndLine(leftLoc.getStartLine() + delta.getSource().size() - 1);
            item.setLeftLocation(leftLoc);

            SourceLocation rightLoc = new SourceLocation();
            rightLoc.setStartLine(delta.getTarget().getPosition() + 1);
            rightLoc.setEndLine(rightLoc.getStartLine() + delta.getTarget().size() - 1);
            item.setRightLocation(rightLoc);

            // 设置内容
            item.setLeftContent(String.join("\n", delta.getSource().getLines()));
            item.setRightContent(String.join("\n", delta.getTarget().getLines()));

            // 判断类型
            switch (delta.getType()) {
                case INSERT:
                    item.setType("ADDED");
                    added++;
                    break;
                case DELETE:
                    item.setType("REMOVED");
                    removed++;
                    break;
                case CHANGE:
                    // 检查是否只是顺序差异
                    if (options.isIgnoreOrder() && isOrderOnlyDiff(
                            delta.getSource().getLines(),
                            delta.getTarget().getLines())) {
                        item.setType("ORDER_ONLY");
                        item.setIgnorable(true);
                        item.setIgnoreReason("仅顺序差异");
                        ignorable++;
                    } else if (isFormatOnlyDiff(
                            delta.getSource().getLines(),
                            delta.getTarget().getLines())) {
                        item.setType("FORMAT_ONLY");
                        item.setIgnorable(true);
                        item.setIgnoreReason("仅格式差异");
                        ignorable++;
                    } else {
                        item.setType("MODIFIED");
                        modified++;
                    }
                    break;
            }

            result.getItems().add(item);
        }

        // 设置摘要
        DiffSummary summary = result.getSummary();
        summary.setAdded(added);
        summary.setRemoved(removed);
        summary.setModified(modified);
        summary.setIgnorableDiffs(ignorable);
        summary.setSignificantDiffs(added + removed + modified - ignorable);
        summary.setTotalDiffs(added + removed + modified);

        return result;
    }

    private String removeComments(String content) {
        // 移除单行注释
        content = SINGLE_LINE_COMMENT.matcher(content).replaceAll("");
        // 移除多行注释
        content = MULTI_LINE_COMMENT.matcher(content).replaceAll("");
        return content;
    }

    private String normalizeHexNumbers(String content) {
        // 标准化十六进制数值：0x01 -> 0x1
        Matcher matcher = HEX_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            // 去除前导零
            hex = hex.replaceFirst("^0+", "");
            if (hex.isEmpty()) {
                hex = "0";
            }
            matcher.appendReplacement(sb, "0x" + hex);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private List<String> normalizeWhitespace(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            // 规范化空白：多个空白变成单个空格，去除首尾空白
            String normalized = line.replaceAll("\\s+", " ").trim();
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    /**
     * 检查是否仅是顺序差异
     * 例如 {A, B, C} 和 {C, B, A} 应该被识别为顺序差异
     */
    private boolean isOrderOnlyDiff(List<String> leftLines, List<String> rightLines) {
        String leftContent = String.join(" ", leftLines);
        String rightContent = String.join(" ", rightLines);

        // 提取数组元素
        Set<String> leftElements = extractArrayElements(leftContent);
        Set<String> rightElements = extractArrayElements(rightContent);

        // 如果元素集合相同，则只是顺序差异
        if (!leftElements.isEmpty() && leftElements.equals(rightElements)) {
            return true;
        }

        // 检查简单的值列表
        Set<String> leftValues = extractValues(leftContent);
        Set<String> rightValues = extractValues(rightContent);

        return !leftValues.isEmpty() && leftValues.equals(rightValues);
    }

    private Set<String> extractArrayElements(String content) {
        Set<String> elements = new HashSet<>();
        Matcher matcher = ARRAY_ELEMENT_PATTERN.matcher(content);
        while (matcher.find()) {
            // 规范化元素内容
            String element = matcher.group(1).trim()
                    .replaceAll("\\s+", " ");
            elements.add(element);
        }
        return elements;
    }

    private Set<String> extractValues(String content) {
        Set<String> values = new HashSet<>();

        // 简单地按逗号分割并规范化
        String cleaned = content.replaceAll("[{}\\[\\]]", "");
        for (String part : cleaned.split(",")) {
            String value = part.trim().replaceAll("\\s+", " ");
            if (!value.isEmpty()) {
                values.add(value);
            }
        }

        return values;
    }

    /**
     * 检查是否仅是格式差异
     */
    private boolean isFormatOnlyDiff(List<String> leftLines, List<String> rightLines) {
        String leftNormalized = String.join("", leftLines)
                .replaceAll("\\s+", "")
                .toLowerCase();
        String rightNormalized = String.join("", rightLines)
                .replaceAll("\\s+", "")
                .toLowerCase();

        return leftNormalized.equals(rightNormalized);
    }
}
