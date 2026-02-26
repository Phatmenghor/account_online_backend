package com.internal.config;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.javacpp.Loader;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class OpenCVConfig {

    @PostConstruct
    public void loadOpenCV() {
        try {
            Loader.load(opencv_core.class);
            log.info("OpenCV loaded successfully via bytedeco javacv");
        } catch (Exception e) {
            log.error("Failed to load OpenCV: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot initialize OpenCV", e);
        }
    }
}