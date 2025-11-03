package ru.skillfactorydemo.tgbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TgBotApplicationTests {

	@Test
	void contextLoads() {
		// Пустой тест — проверяет, что приложение запускается
	}
}
