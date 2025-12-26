package com.cfileprocessor.controller;

import com.cfileprocessor.model.dto.RuleExecuteRequest;
import com.cfileprocessor.model.entity.RuleDefinition;
import com.cfileprocessor.model.vo.ApiResponse;
import com.cfileprocessor.model.vo.CheckResult;
import com.cfileprocessor.service.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RuleController {

    private final RuleService ruleService;

    /**
     * 获取规则列表
     */
    @GetMapping
    public ApiResponse<List<RuleDefinition>> listRules(
            @RequestParam(required = false) String status) {
        List<RuleDefinition> rules = ruleService.listRules(status);
        return ApiResponse.success(rules);
    }

    /**
     * 获取单个规则
     */
    @GetMapping("/{id}")
    public ApiResponse<RuleDefinition> getRule(@PathVariable Long id) {
        RuleDefinition rule = ruleService.getById(id);
        if (rule != null) {
            return ApiResponse.success(rule);
        } else {
            return ApiResponse.error(404, "规则未找到");
        }
    }

    /**
     * 创建规则
     */
    @PostMapping
    public ApiResponse<RuleDefinition> createRule(@RequestBody RuleDefinition rule) {
        try {
            RuleDefinition created = ruleService.createRule(rule);
            return ApiResponse.success(created);
        } catch (Exception e) {
            log.error("创建规则失败", e);
            return ApiResponse.error("创建规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新规则
     */
    @PutMapping("/{id}")
    public ApiResponse<RuleDefinition> updateRule(
            @PathVariable Long id,
            @RequestBody RuleDefinition rule) {
        try {
            rule.setId(id);
            ruleService.updateById(rule);
            return ApiResponse.success(ruleService.getById(id));
        } catch (Exception e) {
            log.error("更新规则失败", e);
            return ApiResponse.error("更新规则失败: " + e.getMessage());
        }
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteRule(@PathVariable Long id) {
        boolean success = ruleService.removeById(id);
        return ApiResponse.success(success);
    }

    /**
     * 发布规则
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Boolean> publishRule(@PathVariable Long id) {
        boolean success = ruleService.publishRule(id);
        return ApiResponse.success(success);
    }

    /**
     * 执行规则检查
     */
    @PostMapping("/execute")
    public ApiResponse<CheckResult> executeRules(@RequestBody RuleExecuteRequest request) {
        try {
            CheckResult result = ruleService.executeRules(
                    request.getRuleIds(),
                    request.getContent(),
                    request.getFileName()
            );
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("执行规则检查失败", e);
            return ApiResponse.error("规则检查失败: " + e.getMessage());
        }
    }
}
