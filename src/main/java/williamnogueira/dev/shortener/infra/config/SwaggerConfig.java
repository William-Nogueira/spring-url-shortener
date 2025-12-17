package williamnogueira.dev.shortener.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        SpringDocUtils.getConfig().addResponseTypeToIgnore(RedirectView.class);

        var devServer = new Server();
        devServer.setDescription("Developer documentation");

        var contact = new Contact();
        contact.setName("William Nogueira");
        contact.setUrl("https://www.linkedin.com/in/william-nogueira-dev");

        var info = new Info()
                .title("URL Shortener API")
                .version("1.0")
                .contact(contact)
                .description("URL shortener API built with Spring Boot, Redis, and DynamoDB.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
