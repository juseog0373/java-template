package com.nexacode.template.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PageResponse<T> extends BaseResponse {
    private int totalPage;     // 총 페이지 수
    private long totalCount; // 총 데이터 수
    private List<T> data;       // 응답 데이터 리스트

    public PageResponse(List<T> data, int totalPage, long totalCount) {
        setSuccess();
        this.data = data;
        this.totalPage = totalPage;
        this.totalCount = totalCount;
    }

    public static <T, U> PageResponse<U> success(Page<T> page, List<U> data) {
        return new PageResponse<>(data, page.getTotalPages(), page.getTotalElements());
    }
}
