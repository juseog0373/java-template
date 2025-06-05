package com.nexacode.template.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.stream.Collectors;

public class QueryUtils {

    /**
     * 공통 페이징
     */
    public static Pageable getPageable(int page, int size, String... sortFields) {
        Sort sort = Sort.by(
                Arrays.stream(sortFields)
                        .map(field -> Sort.Order.desc(field))
                        .collect(Collectors.toList())
        );
        return PageRequest.of(page, size, sort);
    }
}
