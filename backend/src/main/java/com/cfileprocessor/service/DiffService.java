package com.cfileprocessor.service;

import com.cfileprocessor.engine.diff.DiffEngine;
import com.cfileprocessor.model.dto.DiffRequest;
import com.cfileprocessor.model.vo.DiffResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiffService {

    private final DiffEngine diffEngine;

    public DiffResult compare(String leftContent, String rightContent, DiffRequest.DiffOptions options) {
        log.info("执行文件比对, 选项: ignoreWhitespace={}, ignoreOrder={}",
                options != null ? options.isIgnoreWhitespace() : true,
                options != null ? options.isIgnoreOrder() : true);
        return diffEngine.compare(leftContent, rightContent, options);
    }
}
