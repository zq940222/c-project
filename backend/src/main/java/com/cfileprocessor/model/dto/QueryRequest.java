package com.cfileprocessor.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class QueryRequest {
    private String dsl;
    private List<String> fileIds;
}
