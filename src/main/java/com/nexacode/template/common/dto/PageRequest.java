package com.nexacode.template.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {
    @Schema(description = "페이지 번호 (0부터 시작)", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "페이지당 데이터 수", defaultValue = "10")
    private Integer size = 10;
}
