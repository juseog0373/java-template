package com.nexacode.template.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BaseResponse<T> {

    @Schema(description = "상태코드", example = "200")
    private Integer code;

    @Schema(description = "메세지", example = "SUCCESS")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public BaseResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "SUCCESS", data);
    }

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(200, "SUCCESS", null);
    }
    
    public void setSuccess() {
        this.code = 200;
        this.message = "SUCCESS";
    }
}

