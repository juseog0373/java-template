package com.nexacode.template.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
    @Schema(description = "원본 파일명")
    private String originalName;

    @Schema(description = "파일 경로(URL)")
    private String filePath;
}