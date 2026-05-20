package com.starkIndustries.serviceJourney.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer{

  @Autowired
  public RequestInterceptor requestInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestInterceptor)
      .addPathPatterns("/**")
      .excludePathPatterns(
        "/auth/signup",
        "/auth/login",
        "/session/start",
        "/health"
      );
  }
  
}
