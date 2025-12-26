package com.cfileprocessor.controller;

import com.cfileprocessor.model.dto.DiffRequest;
import com.cfileprocessor.model.vo.ApiResponse;
import com.cfileprocessor.model.vo.DiffResult;
import com.cfileprocessor.service.DiffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/diff")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiffController {

    private final DiffService diffService;

    /**
     * 执行文件比对
     */
    @PostMapping
    public ApiResponse<DiffResult> compare(@RequestBody DiffRequest request) {
        try {
            DiffResult result = diffService.compare(
                    request.getLeftContent(),
                    request.getRightContent(),
                    request.getOptions()
            );
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("文件比对失败", e);
            return ApiResponse.error("比对失败: " + e.getMessage());
        }
    }
}
