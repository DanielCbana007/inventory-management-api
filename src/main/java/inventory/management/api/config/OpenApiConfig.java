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
    public OpenAPI customOpenAPI (){
        return new OpenAPI()
                .info( new Info()
                        .title("Inventory management.")
                        .version("1.0.0")
                        .description("API REST para la gestión de un inventario. "
                                + "Los errores siguen el RFC 9457 (Problem Details).")
                        .contact(new Contact()
                                .name("Daniel Andres Cabana Trejos")
                                .email("danielcabana0727@gmail.com")
                                .url("https://github.com/DanielCbana007"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                        )
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("local")
                ));
    }
}
