package com.nexacode.template.common.entity;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileJson {
    private String originalName;
    private String filePath;
}