package de.skuld.web.configuration;

import javax.annotation.Generated;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")
@Configuration
public class SwaggerUiConfiguration implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.
        addResourceHandler("/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
        .resourceChain(false);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/swagger-ui/").setViewName("forward:/swagger-ui/index.html");
  }
}
