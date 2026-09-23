package inventory.management.api;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(ApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}