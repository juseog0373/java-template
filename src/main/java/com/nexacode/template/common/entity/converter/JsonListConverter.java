package com.nexacode.template.common.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexacode.template.common.entity.FileJson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter()
public class JsonListConverter implements AttributeConverter<List<FileJson>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<FileJson> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert list of File to JSON", e);
        }
    }

    @Override
    public List<FileJson> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<FileJson>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Could not convert JSON to list of FileJson", e);
        }
    }
}