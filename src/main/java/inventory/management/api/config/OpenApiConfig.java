package inventory.management.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Management API")
                        .version("1.0.0")
                        .description("""
                                REST API for inventory management.

                                Errors are returned as **RFC 9457 (Problem Details)** with
                                `type`, `title`, `status` and `detail`. Validation responses add
                                an `errors` property holding one object per field with `field`,
                                `code` and `message`.

                                `code` is stable (`NotBlank`, `Size`); `message` changes with the
                                `Accept-Language` header, so clients must not branch on it.""")
                        .contact(new Contact()
                                .name("Daniel Andres Cabana Trejos")
                                .url("https://github.com/DanielCbana007"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local environment (dev profile)")));
    }
}
