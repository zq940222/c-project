package com.cfileprocessor.service;

import com.cfileprocessor.engine.parser.CParser;
import com.cfileprocessor.engine.query.QueryEngine;
import com.cfileprocessor.model.vo.ParseResult;
import com.cfileprocessor.model.vo.QueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {

    private final QueryEngine queryEngine;
    private final CParser cParser;
    private final FileService fileService;

    public QueryResult executeQuery(String dsl, String content, String fileName) {
        // 解析内容
        ParseResult parseResult = cParser.parse(content, fileName);
        // 执行查询
        return queryEngine.execute(dsl, parseResult);
    }

    public QueryResult executeQueryWithFileId(String dsl, String fileId) {
        ParseResult parseResult = fileService.getParseResult(fileId);
        if (parseResult == null) {
            QueryResult result = new QueryResult();
            log.warn("文件未找到: {}", fileId);
            return result;
        }
        return queryEngine.execute(dsl, parseResult);
    }
}
