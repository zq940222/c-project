package com.cfileprocessor.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("struct_definition")
public class StructDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String fields; // JSON格式

    private String sourceFile;

    private String sourceLocation; // JSON格式

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
