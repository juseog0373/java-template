package com.nexacode.template.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevNextResponse {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "제목")
    private String title;
}
