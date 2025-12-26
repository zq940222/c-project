package com.cfileprocessor.model.dto;

import lombok.Data;

@Data
public class ParseRequest {
    private String content;
    private String fileName;
}
