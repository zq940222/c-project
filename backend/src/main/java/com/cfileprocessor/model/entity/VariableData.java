package com.cfileprocessor.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("variable_data")
public class VariableData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long structId;

    private String name;

    private Boolean isArray;

    private String data; // JSON格式

    private String sourceFile;

    private String sourceLocation; // JSON格式

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
