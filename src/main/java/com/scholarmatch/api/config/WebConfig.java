package com.scholarmatch.api.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final String uploadsRoot;

  public WebConfig(@Value("${app.uploads.root}") String uploadsRoot) {
    this.uploadsRoot = uploadsRoot;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = Path.of(uploadsRoot).toAbsolutePath().toUri().toString();
    registry.addResourceHandler("/uploads/**").addResourceLocations(location);
  }
}
