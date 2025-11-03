package ru.skillfactorydemo.tgbot;

import org.springframework.boot.SpringApplication;

public class TestTgbotApplication {

	public static void main(String[] args) {
		SpringApplication.from(TgbotApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
