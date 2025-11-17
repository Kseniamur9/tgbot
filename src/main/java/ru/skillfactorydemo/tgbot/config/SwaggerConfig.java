package ru.skillfactorydemo.tgbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI botApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Telegram Currency & Finance Bot API")
                        .version("1.0.0")
                        .description("REST API для Telegram-бота: курсы валют, доходы, расходы")
                        .contact(new Contact()
                                .name("SkillFactory Student")
                                .email("student@skillfactory.ru"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}