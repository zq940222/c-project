package com.cfileprocessor.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cfileprocessor.model.entity.RuleDefinition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleDefinitionMapper extends BaseMapper<RuleDefinition> {
}
