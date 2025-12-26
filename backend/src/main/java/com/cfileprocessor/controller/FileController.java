package com.cfileprocessor.controller;

import com.cfileprocessor.model.dto.ParseRequest;
import com.cfileprocessor.model.vo.ApiResponse;
import com.cfileprocessor.model.vo.ParseResult;
import com.cfileprocessor.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/v1/parse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    /**
     * 上传并解析文件
     */
    @PostMapping("/upload")
    public ApiResponse<ParseResult> uploadAndParse(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileName = file.getOriginalFilename();
            ParseResult result = fileService.parseContent(content, fileName);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("文件上传解析失败", e);
            return ApiResponse.error("文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析文本内容
     */
    @PostMapping("/content")
    public ApiResponse<ParseResult> parseContent(@RequestBody ParseRequest request) {
        try {
            ParseResult result = fileService.parseContent(request.getContent(), request.getFileName());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("内容解析失败", e);
            return ApiResponse.error("解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取已解析的文件信息
     */
    @GetMapping("/{fileId}")
    public ApiResponse<ParseResult> getParseResult(@PathVariable String fileId) {
        ParseResult result = fileService.getParseResult(fileId);
        if (result != null) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(404, "文件未找到");
        }
    }
}
