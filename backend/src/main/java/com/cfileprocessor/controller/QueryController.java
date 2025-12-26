package com.cfileprocessor.controller;

import com.cfileprocessor.model.dto.QueryRequest;
import com.cfileprocessor.model.vo.ApiResponse;
import com.cfileprocessor.model.vo.QueryResult;
import com.cfileprocessor.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/query")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QueryController {

    private final QueryService queryService;

    /**
     * 执行查询（基于内容）
     */
    @PostMapping
    public ApiResponse<QueryResult> executeQuery(@RequestBody QueryRequest request) {
        try {
            QueryResult result;
            if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
                // 基于已解析的文件
                result = queryService.executeQueryWithFileId(request.getDsl(), request.getFileIds().get(0));
            } else {
                return ApiResponse.error("请提供fileId或content");
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询执行失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 执行查询（带内容）
     */
    @PostMapping("/content")
    public ApiResponse<QueryResult> executeQueryWithContent(
            @RequestParam String dsl,
            @RequestBody String content) {
        try {
            QueryResult result = queryService.executeQuery(dsl, content, "query.c");
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询执行失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
}
