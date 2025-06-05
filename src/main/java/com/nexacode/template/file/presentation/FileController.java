package com.nexacode.template.file.presentation;

import com.nexacode.template.common.dto.BaseResponse;
import com.nexacode.template.file.application.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "file", description = "파일 관련 API")
public class FileController {
    private final FileService fileService;

    @Operation(
            summary = "공통 파일 업로드 API",
            description = "파일을 s3 에 업로드 합니다. 이후 s3 객체 url, 파일 originalName 을 리턴해줍니다. <br>" +
                    "해당 url 를 imagePath 또는 filePath 의 값으로 사용하시면 됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<FileResponse.GetFile> uploadFile(@RequestPart("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            return BaseResponse.success(fileService.uploadFile(file, originalName, fileName));
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "파일 다운로드 API", description = "파일 id 또는 s3 url 을 통해 파일을 다운로드합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(@ParameterObject @ModelAttribute FileRequest.DownloadFile request) {
        return fileService.downloadFile(request);
    }
}