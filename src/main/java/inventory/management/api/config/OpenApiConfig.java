package inventory.management.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class OpenApiConfig {

    // La version sale de build.gradle (springBoot { buildInfo() }), no de un literal aqui.
    // Si la app arranca sin pasar por Gradle no hay build-info, y se muestra "dev".
    @Bean
    public OpenAPI customOpenAPI(ObjectProvider<BuildProperties> buildProperties) {
        String version = Optional.ofNullable(buildProperties.getIfAvailable())
                .map(BuildProperties::getVersion)
                .orElse("dev");

        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Management API")
                        .version(version)
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
                // MEJORA [§3.4]: solo hay un server y es localhost:8080. En cuanto se despliegue,
                //         la Swagger UI publicada mandara las peticiones a la maquina del que
                //         la abra.
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local environment (dev profile)")));
    }
}
