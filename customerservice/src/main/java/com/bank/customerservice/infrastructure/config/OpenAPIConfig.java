package com.bank.customerservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Configuration: OpenAPI / Swagger
 * Define la información y configuración de la documentación API
 */
@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Customer Service API")
                .version("1.0.0")
                .description("API para gestión integral de clientes y personas en el sistema bancario")
                .contact(new Contact()
                    .name("Banking Microservices")
                    .url("https://github.com/sofka/banking-microservices")
                )
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                )
            );
    }
}
