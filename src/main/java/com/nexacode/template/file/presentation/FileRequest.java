package com.nexacode.template.file.presentation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class FileRequest {

    @Getter
    @Setter
    @Schema(name = "DownloadFileRequest")
    public static class DownloadFile {
        @Parameter(description = "파일 s3 url")
        @NotBlank(message = "파일 url 은 필수입니다")
        private String filePath;
    }
}

