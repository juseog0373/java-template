package com.nexacode.template.file.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class FileResponse {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @Schema(name = "GetFileResponse")
    public static class GetFile {
        @Schema(description = "파일 원본 이름")
        private String originalName;

        @Schema(description = "s3 url")
        private String filePath;
    }
}
