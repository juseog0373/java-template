package com.nexacode.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
public class JavaTemplateApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(JavaTemplateApplication.class, args);
        log.info("LocalDateTime now: {}", LocalDateTime.now());

        log.info("ZoneId: {}", ZoneId.systemDefault().getId());
    }

}
