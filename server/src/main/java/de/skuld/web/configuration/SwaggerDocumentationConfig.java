package de.skuld.web.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import javax.annotation.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")
@Configuration
public class SwaggerDocumentationConfig {

  @Bean
  public Docket customImplementation() {
    return new Docket(DocumentationType.OAS_30)
        .select()
        .apis(RequestHandlerSelectors.basePackage("de.skuld.web.api"))
        .build()
        .apiInfo(apiInfo());
  }

  ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Skuld API")
        .description("API for the skuld webservice")
        .license("MIT License")
        .licenseUrl("https://github.com/bastihav/skuld/blob/main/LICENSE")
        .termsOfServiceUrl("")
        .version("1.0.0")
        .contact(new Contact("", "", "bastihav@mail.uni-paderborn.de"))
        .build();
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Skuld API")
            .description("API for the skuld webservice")
            .termsOfService("")
            .version("1.0.0")
            .license(new License()
                .name("MIT License")
                .url("https://github.com/bastihav/skuld/blob/main/LICENSE"))
            .contact(new io.swagger.v3.oas.models.info.Contact()
                .email("bastihav@mail.uni-paderborn.de")));
  }

}
