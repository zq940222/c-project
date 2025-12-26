package com.cfileprocessor.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Data
public class QueryResult {
    private List<String> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();
    private int totalCount;
    private long executionTime;
}
