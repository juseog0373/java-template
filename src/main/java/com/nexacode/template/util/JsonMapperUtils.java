package com.nexacode.template.util;


import com.nexacode.template.common.dto.FileDto;
import com.nexacode.template.common.entity.FileJson;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JsonMapperUtils {

    // DTO -> Entity용 FileJson 변환
    public static List<FileJson> dtoToEntity(List<FileDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return Collections.emptyList();
        return dtoList.stream()
                .map(dto -> FileJson.builder()
                        .originalName(dto.getOriginalName())
                        .filePath(dto.getFilePath())
                        .build())
                .collect(Collectors.toList());
    }

    // Entity용 FileJson -> DTO 변환
    public static List<FileDto> entityToDto(List<FileJson> entityList) {
        if (entityList == null || entityList.isEmpty()) return Collections.emptyList();
        return entityList.stream()
                .map(json -> FileDto.builder()
                        .originalName(json.getOriginalName())
                        .filePath(json.getFilePath())
                        .build())
                .collect(Collectors.toList());
    }
}
