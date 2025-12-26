package com.cfileprocessor.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rule_definition")
public class RuleDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String ruleType;

    private String dslContent;

    private String targetVariable;

    private String targetField;

    private String status; // DRAFT, PUBLISHED, DISABLED

    private String severity; // ERROR, WARNING, INFO

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
