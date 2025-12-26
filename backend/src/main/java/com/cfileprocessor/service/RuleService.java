package com.cfileprocessor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cfileprocessor.engine.parser.CParser;
import com.cfileprocessor.engine.rule.RuleEngine;
import com.cfileprocessor.model.entity.RuleDefinition;
import com.cfileprocessor.model.vo.CheckResult;
import com.cfileprocessor.model.vo.ParseResult;
import com.cfileprocessor.repository.RuleDefinitionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService extends ServiceImpl<RuleDefinitionMapper, RuleDefinition> {

    private final RuleEngine ruleEngine;
    private final CParser cParser;
    private final FileService fileService;

    public List<RuleDefinition> listRules(String status) {
        LambdaQueryWrapper<RuleDefinition> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RuleDefinition::getStatus, status);
        }
        wrapper.orderByDesc(RuleDefinition::getCreateTime);
        return list(wrapper);
    }

    public RuleDefinition createRule(RuleDefinition rule) {
        if (rule.getStatus() == null) {
            rule.setStatus("DRAFT");
        }
        if (rule.getSeverity() == null) {
            rule.setSeverity("ERROR");
        }
        save(rule);
        log.info("创建规则: {} ({})", rule.getName(), rule.getId());
        return rule;
    }

    public boolean publishRule(Long id) {
        RuleDefinition rule = getById(id);
        if (rule != null) {
            rule.setStatus("PUBLISHED");
            updateById(rule);
            log.info("发布规则: {} ({})", rule.getName(), id);
            return true;
        }
        return false;
    }

    public CheckResult executeRules(List<Long> ruleIds, String content, String fileName) {
        // 解析内容
        ParseResult parseResult = cParser.parse(content, fileName);

        // 获取规则
        List<RuleDefinition> rules;
        if (ruleIds != null && !ruleIds.isEmpty()) {
            rules = listByIds(ruleIds);
        } else {
            // 执行所有已发布规则
            rules = listRules("PUBLISHED");
        }

        // 执行规则检查
        return ruleEngine.execute(rules, parseResult);
    }

    public CheckResult executeRulesWithFileId(List<Long> ruleIds, String fileId) {
        ParseResult parseResult = fileService.getParseResult(fileId);
        if (parseResult == null) {
            CheckResult result = new CheckResult();
            result.getResults().add(createErrorItem("文件未找到: " + fileId));
            return result;
        }

        List<RuleDefinition> rules;
        if (ruleIds != null && !ruleIds.isEmpty()) {
            rules = listByIds(ruleIds);
        } else {
            rules = listRules("PUBLISHED");
        }

        return ruleEngine.execute(rules, parseResult);
    }

    private CheckResult.CheckItem createErrorItem(String message) {
        CheckResult.CheckItem item = new CheckResult.CheckItem();
        item.setResultType("ERROR");
        item.setMessage(message);
        return item;
    }
}
