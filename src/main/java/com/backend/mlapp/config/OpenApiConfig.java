package com.backend.mlapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "admin",
                        email = "nispyrou.se@gmail.com",
                        url = "http://localhost:8080/adminpanel"
                ),
                description = "OpenApi documentation for Cloud Application with machine learning models",
                title = "OpenApi specification",
                version = "1.0",
                license = @License(

                ),
                termsOfService = "Terms of Service"
        ),
                servers = {
                        @Server(
                                description = "Local ENV",
                                url = "http://localhost:8080"
                        )
                }

)
public class OpenApiConfig {
}
