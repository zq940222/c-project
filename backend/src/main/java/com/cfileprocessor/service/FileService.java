package com.cfileprocessor.service;

import com.cfileprocessor.engine.parser.CParser;
import com.cfileprocessor.model.vo.ParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final CParser cParser;

    // 内存缓存解析结果（POC用，生产环境应使用Redis或数据库）
    private final Map<String, ParseResult> parseResultCache = new ConcurrentHashMap<>();

    public ParseResult parseContent(String content, String fileName) {
        ParseResult result = cParser.parse(content, fileName);
        // 缓存结果
        parseResultCache.put(result.getFileId(), result);
        log.info("解析文件 {} 完成, fileId={}", fileName, result.getFileId());
        return result;
    }

    public ParseResult getParseResult(String fileId) {
        return parseResultCache.get(fileId);
    }

    public void clearCache() {
        parseResultCache.clear();
    }
}
