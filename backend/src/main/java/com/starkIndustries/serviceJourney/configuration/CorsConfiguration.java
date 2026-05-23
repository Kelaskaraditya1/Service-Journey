package com.starkIndustries.serviceJourney.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

  @Bean
  public WebMvcConfigurer getCorsConfigurer(){
    return new WebMvcConfigurer() {
      
      @Override
      public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")  // tells to which routes of backend should Cors Configuration should be applied.
          .allowedHeaders("*")
          .allowedMethods("*")
          .allowedOrigins("http://localhost:3000/")
          .allowCredentials(true);

      }
    };
  }
  
}
