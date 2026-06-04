package com.ev.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * 업로드 이미지 외부 폴더 매핑 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/upload/member/profile/**")
                .addResourceLocations(
                        "file:" + System.getProperty("user.dir") + "/upload/member/profile/"
                );
    }
}