package com.java.bank;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Bank Application",
				description = "Backend Rest APIS for Bank",
				version = "v1.0",
				contact = @Contact(
						name = "Rishabh Patel",
						email = "rishabhpatel3005@gmail.com",
						url = "https://github.com/rishabhpatel3005/repo_name"
				),
				license = @License(
						name="Rishabh Patel",
						url = "https://github.com/rishabhpatel3005/repo_name"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "Bank App Documentation",
				url = "https://github.com/rishabhpatel3005/repo_name"
		)
)
public class BankApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankApplication.class, args);
	}

}
